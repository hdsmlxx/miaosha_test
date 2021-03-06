package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.google.common.util.concurrent.RateLimiter;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.mq.MqProducer;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.ItemService;
import com.miaosha.service.OrderService;
import com.miaosha.service.PromoService;
import com.miaosha.service.model.UserModel;
import com.miaosha.utils.CodeUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.RenderedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.*;

@Controller("order")
@RequestMapping("/order")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class OrderController extends BaseController {

    @Autowired
    OrderService orderService;

    @Autowired
    private ItemService itemService;

    @Autowired
    private PromoService promoService;

    @Autowired
    HttpServletRequest httpServletRequest;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private MqProducer mqProducer;
    
    private ExecutorService executorService;

    private RateLimiter orderCreateRateLimiter;

    @PostConstruct
    public void init() {
        executorService = Executors.newFixedThreadPool(20);
        orderCreateRateLimiter = RateLimiter.create(100);
    }

    @RequestMapping(value = "/generateVerifyCode", method = {RequestMethod.POST, RequestMethod.GET})
    @ResponseBody
    public void generateVerifyCode(HttpServletResponse response) throws BusinessException, IOException {
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "???????????????????????????????????????");
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN, "???????????????????????????????????????");
        }

        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        //???????????????????????????
        redisTemplate.opsForValue().set("verify_code_" + userModel.getId(), map.get("code"));
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", response.getOutputStream());
    }


    @RequestMapping(value = "/generatePromoToken", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType generatePromoToken(@RequestParam(name = "itemId") Integer itemId,
                                               @RequestParam(name = "promoId", required = false) Integer promoId,
                                               @RequestParam(name = "verifyCode") String verifyCode) throws BusinessException {

        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //???????????????
        String redisVerifyCode = (String) redisTemplate.opsForValue().get("verify_code_" + userModel.getId());
        if (StringUtils.isEmpty(redisVerifyCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "??????????????????????????????");
        }
        if (!redisVerifyCode.equalsIgnoreCase(verifyCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "???????????????");
        }
        String promoToken = promoService.generateSecondKillToken(promoId, itemId, userModel.getId());
        if (promoToken == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "??????????????????");
        }
        return CommonReturnType.create(promoToken);
    }

    /**
     * ??????????????????
     * @param itemId
     * @param amount
     * @param promoId
     * @param promoToken
     * @return
     * @throws BusinessException
     */
    @RequestMapping(value = "/createOrder", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType createOrder(@RequestParam(name = "itemId") Integer itemId,
                                        @RequestParam(name = "amount") Integer amount,
                                        @RequestParam(name = "promoId", required = false) Integer promoId,
                                        @RequestParam(name = "promoToken", required = false) String promoToken) throws BusinessException {

        //????????????
        if (!orderCreateRateLimiter.tryAcquire()) {
            throw new BusinessException(EmBusinessError.ORDER_CREATE_FAIL);
        }

        // ????????????????????????
        String token = httpServletRequest.getParameterMap().get("token")[0];
        if (StringUtils.isEmpty(token)) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        UserModel userModel = (UserModel) redisTemplate.opsForValue().get(token);
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }

        //??????promoToken
        if (promoId != null) {
            String inRedisPromoToken = (String) redisTemplate.opsForValue().get("promo_token_" + promoId + "_userId_" +
                    userModel.getId() + "_itemId_" + itemId);
            if (inRedisPromoToken == null) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "????????????????????????");
            }
            if (!org.apache.commons.lang3.StringUtils.equals(promoToken, inRedisPromoToken)) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "????????????????????????");
            }
        }

        /*Boolean isLogin = (Boolean) httpServletRequest.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin.booleanValue()) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL, "???????????????");
        }
        // ???????????????service
        UserModel userModel = (UserModel) httpServletRequest.getSession().getAttribute("LOGIN_USER");*/

//        orderService.createOrder(userModel.getId(), promoId, itemId, amount);

        if (redisTemplate.hasKey("item_stock_invalid_" + itemId)) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH, "???????????????");
        }

        //???????????????????????????????????????
        Future<Object> future = executorService.submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //????????????????????????init??????
                String stockLogId = itemService.initStockLog(itemId, amount);

                boolean result = mqProducer.asyncTransactionReduceStock(userModel.getId(), promoId, itemId, amount, stockLogId);
                if (!result) {
                    throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "????????????");
                }
                return null;
            }
        });

        try {
            future.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        } catch (ExecutionException e) {
            e.printStackTrace();
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR);
        }

        return CommonReturnType.create(null);
    }
}

package com.miaosha.controller;

import com.alibaba.druid.util.StringUtils;
import com.miaosha.controller.viewobject.UserVO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.response.CommonReturnType;
import com.miaosha.service.UserService;
import com.miaosha.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

@Controller("user")
@RequestMapping("/user")
// 解决跨域请求的问题
@CrossOrigin(allowCredentials = "true", originPatterns = "*")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    /**
     * 默认单例模式，多用户并发访问怎么和不同用户进行绑定呢？
     * 其本质是proxy，内部有threalocal方式的map，让用户在对应的线程中处理request，
     * 并且有threadlocal清除机制
     */
    @Autowired
    private HttpServletRequest httpServletRequest;

    // 用户登录接口
    @RequestMapping(value = "/login", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {

        // 入参校验
        if (org.apache.commons.lang3.StringUtils.isEmpty(telphone) ||
                org.apache.commons.lang3.StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        // 校验用户登录是否合法
        UserModel userModel = userService.validateLogin(telphone, this.encodeByMd5(password));

        // 将登录凭证加入到用户登录成功的session中
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userModel);

        return CommonReturnType.create(null);
    }

    // 用户注册接口
    @RequestMapping(value = "/register", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") String gender,
                                     @RequestParam(name = "age") String age,
                                     @RequestParam(name = "password") String password) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        // 验证手机号和otpcode是否相符
        String inSessionOtpCode = (String) this.httpServletRequest.getSession().getAttribute(telphone);
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不正确");
        }

        // 用户注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setGender(new Byte(gender));
        userModel.setAge(Integer.valueOf(age));
        userModel.setTelphone(telphone);
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.encodeByMd5(password));

        userService.register(userModel);

        return CommonReturnType.create(null);
    }

    public String encodeByMd5(String string) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();

        String newStr = base64Encoder.encode(messageDigest.digest(string.getBytes("utf-8")));
        return newStr;
    }

    @RequestMapping(value = "/getotp", method = RequestMethod.POST, consumes = CONTENT_TYPE_FORMED)
    @ResponseBody
    public CommonReturnType getOpt(@RequestParam(name = "telphone") String telphone) {
        // 按照一定的规则生成OTP验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        /**
         * 企业级应用中一般使用redis方式关联，
         * 在本项目中使用httpsession进行关联
         */
        // 将OTP验证码和对应用户的手机号关联
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        // 将OTP验证码通过短信通道发送给用户
        System.out.println("telphone = " + telphone + " & otpCode = " + otpCode);

        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    @ResponseBody   //将java对象转为json格式的数据
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        // 调用service服务，获取对应的用户信息返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的对象信息不存在
        if (userModel == null) {
//            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
            userModel.setEncrptPassword("1");
        }

        /**
         * 此处不能直接将核心领域模型model返回给前端，因为会透出安全信息
         */
        //将核心领域模型对象转化为可供UI使用的VO对象
        UserVO userVO = convertFromModel(userModel);
        //返回正确信息
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userModel, userVO);

        return userVO;
    }

}

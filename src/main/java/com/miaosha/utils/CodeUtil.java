package com.miaosha.utils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CodeUtil {

    /**
     * 图片的width
     */
    private static int width = 90;

    /**
     * 图片的height
     */
    private static int height = 20;

    /**
     * 图片显示的验证码个数
     */
    private static int codeCount = 4;

    /**
     * 坐标
     */
    private static int xx = 15;

    /**
     * 字体大小
     */
    private static int fontHeight = 18;

    /**
     *
     */
    private static int codeY = 16;

    /**
     * 验证码组成元素
     */
    private static char[] codeSequence = {'A','B','C','D','E','F','G', 'H','I','J','K','L','M','N','O','P','Q','R','S',
            'T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0'};

    public static Map<String, Object> generateCodeAndPic() {
        //定义图像buffer
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics gd = bufferedImage.getGraphics();
        //创建一个随机数生成器
        Random random = new Random();
        //将图像填充为白色
        gd.setColor(Color.white);
        gd.fillRect(0, 0, width, height);
        //创建字体，字体大小根据图片高度来定
        Font font = new Font("Fixedsys", Font.BOLD, fontHeight);
        //设置字体
        gd.setFont(font);
        //画边框
        gd.setColor(Color.black);
        gd.drawRect(0, 0, width - 1, height - 1);
        //随机40条干扰线
        gd.setColor(Color.black);
        for (int i = 0; i < 30; i++) {
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            int xl = random.nextInt(12);
            int yl = random.nextInt(12);
            gd.drawLine(x, y, x + xl, y + yl);
        }
        //randomCode用于保存随机产生的验证码
        StringBuffer randomCodes = new StringBuffer();
        int red = 0, green = 0, blue = 0;
        //随机产生codeCount个数的验证码
        for (int i = 0; i < codeCount; i++) {
            String randomCode = String.valueOf(codeSequence[random.nextInt(36)]);
            //产生随机颜色
            red = random.nextInt(255);
            green = random.nextInt(255);
            blue = random.nextInt(255);

            gd.setColor(new Color(red, green, blue));
            gd.drawString(randomCode, (i + 1) * xx, codeY);

            //将产生的四个随机数组合在一起
            randomCodes.append(randomCode);
        }

        Map<String, Object> map = new HashMap<>(16);
        map.put("code", randomCodes);
        map.put("codePic", bufferedImage);
        return map;
    }

    public static void main(String[] args) throws IOException {
        //创建文件输出流对象
        OutputStream outputStream = new FileOutputStream("/Users/bengka/work/" + System.currentTimeMillis() + ".jpg");
        Map<String, Object> map = CodeUtil.generateCodeAndPic();
        ImageIO.write((RenderedImage) map.get("codePic"), "jpeg", outputStream);
        System.out.printf("验证码：" + map.get("code"));
    }

}

package com.miaosha;

import com.miaosha.dao.UserDoMapper;
import com.miaosha.dataobject.UserDo;
import org.mybatis.spring.annotation.MapperScan;
import org.mybatis.spring.annotation.MapperScans;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
//自动化配置，springboot启动一个内嵌的tomcat，并加载默认配置
@SpringBootApplication(scanBasePackages = {"com.miaosha"})
//controller
@RestController
@MapperScan("com.miaosha.dao")
public class App 
{

    @Autowired
    private UserDoMapper userDoMapper;

    @RequestMapping("/selectUserBy1")
    public String selectUserBy1() {
        UserDo userDo = userDoMapper.selectByPrimaryKey(1);
        if (userDo == null) {
            return "用户不存在";
        } else {
            return userDo.getName();
        }
    }

    @RequestMapping("/")
    public String home() {
        return "页面";
    }
    // run后，启动了一个web容器
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
        SpringApplication.run(App.class, args);
    }
}

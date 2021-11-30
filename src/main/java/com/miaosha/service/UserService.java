package com.miaosha.service;

import com.miaosha.error.BusinessException;
import com.miaosha.service.model.UserModel;

public interface UserService {

    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;

    /**
     * @param telphone:用户注册手机
     * @param encrptPassword：加密后的密码
     * @throws BusinessException
     */
    UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException;
}

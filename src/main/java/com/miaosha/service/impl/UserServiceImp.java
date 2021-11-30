package com.miaosha.service.impl;

import com.miaosha.dao.UserDoMapper;
import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserDo;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.error.BusinessException;
import com.miaosha.error.EmBusinessError;
import com.miaosha.service.UserService;
import com.miaosha.service.model.UserModel;
import com.miaosha.validator.ValidationResult;
import com.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImp implements UserService {

    @Autowired
    private UserDoMapper userDoMapper;

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserModel getUserById(Integer id) {
        UserDo userDo = userDoMapper.selectByPrimaryKey(id);

        if (userDo == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDo.getId());

        UserModel userModel = convertFromDataObject(userDo, userPasswordDO);

        return userModel;
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        /*if (StringUtils.isEmpty(userModel.getName()) ||
            userModel.getGender() == null ||
            userModel.getAge() == null ||
            StringUtils.isEmpty(userModel.getTelphone())) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "入参不合法");
        }*/

        ValidationResult result = validator.validate(userModel);
        if (result.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, result.getErrMsg());
        }

        UserDo userDo = convertFromModel(userModel);

        try {
            // 使用insertSelective，对为空的字段完全依赖于数据库的默认值
            userDoMapper.insertSelective(userDo);
        } catch (DuplicateKeyException ex) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已重复注册");
        }

        // 自增id，返回之后把值赋给userPasswordDO
        userModel.setId(userDo.getId());

        UserPasswordDO userPasswordDO = convertPasswordFromModel(userModel);
        userPasswordDOMapper.insertSelective(userPasswordDO);

    }

    @Override
    public UserModel validateLogin(String telphone, String encrptPassword) throws BusinessException {
        // 通过用户手机获取用户信息
        UserDo userDo = userDoMapper.selectByTelphone(telphone);
        if (userDo == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDo.getId());
        UserModel userModel = convertFromDataObject(userDo, userPasswordDO);

        // 比对用户信息的加密密码是否和传入的密码相匹配
        if (!StringUtils.equals(encrptPassword, userModel.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }

        // 为什么使用return;
        return userModel;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        BeanUtils.copyProperties(userModel, userPasswordDO);
        userPasswordDO.setUserId(userModel.getId());
        return userPasswordDO;
    }

    private UserDo convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        }
        UserDo userDo = new UserDo();
        BeanUtils.copyProperties(userModel, userDo);
        return userDo;
    }

    private UserModel convertFromDataObject(UserDo userDo, UserPasswordDO userPasswordDO) {

        if (userDo == null) {
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDo, userModel);

        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}

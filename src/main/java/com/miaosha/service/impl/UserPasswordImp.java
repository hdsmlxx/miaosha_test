package com.miaosha.service.impl;

import com.miaosha.dao.UserPasswordDOMapper;
import com.miaosha.dataobject.UserPasswordDO;
import com.miaosha.service.UserPasswordService;
import org.springframework.beans.factory.annotation.Autowired;

public class UserPasswordImp implements UserPasswordService {

    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserPasswordDO getUsesrPassword(Integer id) {
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByPrimaryKey(id);
        return userPasswordDO;
    }
}

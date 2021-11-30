package com.miaosha.error;

public enum EmBusinessError implements CommonError {

    //通用错误类型，避免重复定义大量由于参数不合法导致的错误类型
    PARAMETER_VALIDATION_ERROR(10001, "参数不合法"),
    UNKNOWN_ERROR(10002, "未知错误"),

    //20000开头为用户信息错误
    USER_NOT_EXIST(20001, "用户不存在"),
    USER_LOGIN_FAIL(20001, "用户名或密码错误"),

    // 30000开头为商品信息错误
    ITEM_NOT_EXIST(30001, "商品不存在"),

    // 40000开头为交易信息错误
    STOCK_NOT_ENOUGH(40001, "库存不足"),
    ;

    private int errCode;
    private String errMsg;

    private EmBusinessError(int errCode, String errMsg) {
        this.errCode = errCode;
        this.errMsg = errMsg;
    }

    @Override
    public int getErrorCode() {
        return errCode;
    }

    @Override
    public String getErrMsg() {
        return errMsg;
    }

    /**
     * 自定义错误信息描述
     * @param errMsg
     * @return
     */
    @Override
    public CommonError setErrMsg(String errMsg) {
        this.errMsg = errMsg;
        return this;
    }
}

package com.mmall.common;

/**
 * 响应状态枚举类
 */
public enum ResponseCode {

    /**
     * 成功
     */
    SUCCESS(0,"成功"),
    /**
     * 失败
     */
    ERROR(1,"失败"),

    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARGUMENT");

    private final  int code;
    private final  String desc;

    ResponseCode(int code,String desc){
        this.code=code;
        this.desc=desc;
    }

    public int getCode(){
        return code;
    }

    public String getDesc(){
        return desc;
    }
}

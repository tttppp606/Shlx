package com.mmall.common;

/**
 * Created by tttppp606 on 2019/1/25.
 */
public enum ResponseCode {

    SUCCESS(0,"SUCCESS"),
    ERROR(1,"ERROR"),
    NEED_LOGIN(10,"NEED_LOGIN"),
    ILLEGAL_ARGUMENT(2,"ILLEGAL_ARDUMENT");

    //code对应0、1、10、2=status，desc对应状态的说明，相当于默认的msg，但一般msg会自己编写说明
    private final int code;
    private final String desc;

    ResponseCode(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }

    /**
     * 根据code，返回value
     * @param code
     * @return
     */
    public static ResponseCode codeof(int code){
        for (ResponseCode responseCode : values()) {
            if (responseCode.getCode() == code){
                return responseCode;
            }
        }
        throw new RuntimeException("没有找到对应的枚举");
    }
}

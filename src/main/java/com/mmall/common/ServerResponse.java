package com.mmall.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * 返回的数据包括三类参数status，msg和data，将这三个放入一个类中封装，直接返回前台这个类的对象可以
 * Created by tttppp606 on 2019/1/25.
 */
//保证序列化json中，不包含值为null的key
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ServerResponse<T> {
    private int status;
    private String msg;
    private T data;
//    为了让外部不能new这个类，变为私有，随后会有public方法来获得这个类的对象
    private ServerResponse(int status) {
        this.status = status;
    }
    private ServerResponse(int status, String msg) {
        this.status = status;
        this.msg=msg;
    }
    private ServerResponse(int status, T data) {
        this.status = status;
        this.data=data;
    }
    private ServerResponse(int status, String msg, T data) {
//        返回的状态0、1、10、2
        this.status = status;
//        返回的信息，自己指定
        this.msg=msg;
//        返回的数据，数据库中查询的结果
        this.data=data;
    }

    public int getStatus() {
        return status;
    }
    public String getMsg() {
        return msg;
    }
    public T getData() {
        return data;
    }

//   判断操作数据库是否成功的方法
    @JsonIgnore//为了不让这个方法的返回值出现在序列化json里
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();
    }

    // 创建这些方法的目的：因为这个类的构造方法被私有了，为了外界能获得这个类的对象，才创建了这些方法
    public static <T> ServerResponse<T> createBySuccess(){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());
    }
    public static <T> ServerResponse<T> createBySuccessMessage(String msg){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }
    public static <T> ServerResponse<T> createBySuccess(T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);
    }
    public static <T> ServerResponse<T> createBySuccess(String msg, T data){
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }
    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode());
    }
    public static <T> ServerResponse<T> createByErrorMessage(String msg){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),msg);
    }
    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode, String errorMessage){
        return new ServerResponse<T>(errorCode,errorMessage);
    }

}

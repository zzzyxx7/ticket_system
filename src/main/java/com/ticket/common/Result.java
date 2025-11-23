package com.ticket.common;

/**
 * 统一结果返回封装类
 * 用于封装接口返回结果，包含状态码、消息和数据
 * @param <T> 数据类型泛型参数
 */
public class Result<T> {
    private int code;
    private String message;
    private T data;

    /**
     * 创建成功的Result实例
     * @param <T> 数据类型泛型参数
     * @param data 成功时返回的数据
     * @return 包含成功状态码(200)和指定数据的Result实例
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    /**
     * 创建错误的Result实例
     * @param <T> 数据类型泛型参数
     * @param message 错误消息
     * @return 包含错误状态码(500)和指定错误消息的Result实例
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    /**
     * 获取状态码
     * @return 状态码
     */
    public int getCode() {
        return code;
    }

    /**
     * 设置状态码
     * @param code 状态码
     */
    public void setCode(int code) {
        this.code = code;
    }

    /**
     * 获取数据
     * @return 数据
     */
    public T getData() {
        return data;
    }

    /**
     * 设置数据
     * @param data 数据
     */
    public void setData(T data) {
        this.data = data;
    }

    /**
     * 获取消息
     * @return 消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 设置消息
     * @param message 消息
     */
    public void setMessage(String message) {
        this.message = message;
    }
}

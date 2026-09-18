package com.yking.mallcommon;

import lombok.Data;

/**
 * 统一 API 响应结果封装类
 *
 * 所有后端接口统一返回此格式，前端通过 Axios 响应拦截器自动解包：
 * {
 *   "code": 200,           // 状态码：200/0 表示成功，其他表示失败
 *   "message": "success",  // 提示信息
 *   "data": { ... }        // 业务数据（泛型 T）
 * }
 *
 * 使用方式：
 *   Result.success(data)        → 返回成功（带数据）
 *   Result.success()            → 返回成功（无数据）
 *   Result.error("错误信息")    → 返回失败（500）
 *   Result.error(400, "错误")   → 返回失败（自定义状态码）
 *
 * @param <T> 业务数据的类型
 */
@Data
public class Result<T> {
    /** 状态码：200 或 0 表示成功，其他值表示失败 */
    private int code;

    /** 提示信息：成功时通常为 "success"，失败时为错误描述 */
    private String message;

    /** 业务数据：成功时返回的具体数据，泛型 T */
    private T data;

    /** 私有构造方法，通过静态工厂方法创建实例 */
    private Result(int code, String message, T data) {
        this.code = code; // 响应码
        this.message = message; // 提示信息
        this.data = data;
    }

    /** 返回成功结果（无数据） */
    public static <T> Result<T> success() {
        return new Result<>(200, "success", null);
    }

    /** 返回成功结果（带数据） */
    public static <T> Result<T> success(T data) {
        return new Result<>(200, "success", data);
    }

    /** 返回失败结果（自定义状态码和消息） */
    public static <T> Result<T> error(int code, String message) {
        return new Result<>(code, message, null);
    }

    /** 返回失败结果（默认 500 状态码） */
    public static <T> Result<T> error(String message) {
        return new Result<>(500, message, null);
    }
}

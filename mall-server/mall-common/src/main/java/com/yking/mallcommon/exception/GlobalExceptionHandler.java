package com.yking.mallcommon.exception;

import com.yking.mallcommon.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    /**
     * 处理业务异常
     * 返回业务定义的 code 和 message，HTTP 状态码仍为 200
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }
    /**
     * 处理 @Valid 参数校验失败
     * 提取所有字段校验错误拼接为一条消息返回
     * 例如："name: 请输入商品名称; price: 价格不能为空"
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数校验失败");
        return Result.error(400, msg);
    }
    /**
     * 处理参数绑定失败（如 URL 参数类型不匹配）
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String msg = e.getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("参数绑定失败");
        return Result.error(400, msg);
    }
    /**
     * 处理权限不足异常（用户无权限访问该接口）
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        return Result.error(403, "没有权限访问");
    }

    /**
     * 处理访问了不存在的地址
     *
     * 没有匹配的 Controller 时，请求会落到静态资源处理器并抛出 NoResourceFoundException。
     * 不单独接住的话，它会被下面的兜底当成系统异常，对外返回 500、
     * 对内打印一整条堆栈，把真正的故障淹没在日志里。
     */
    @ExceptionHandler(NoResourceFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Void> handleNoResourceFoundException(NoResourceFoundException e, HttpServletRequest request) {
        log.warn("访问了不存在的地址: {}", request.getRequestURI());
        return Result.error(404, "请求的地址不存在");
    }

    /**
     * 兜底异常处理 — 捕获所有未处理的异常
     * 记录完整堆栈日志用于排查，但只给前端返回通用错误提示（避免泄露系统信息）
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统异常 [{}]: {}", request.getRequestURI(), e.getMessage(), e);
        return Result.error(500, "服务器内部错误");
    }
}

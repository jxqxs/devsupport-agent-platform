package com.example.devsupport.common;

/** 统一返回。成功时携带数据，失败时携带错误码。 */
public class Result<T> {
    private final boolean success;
    private final T data;
    private final ErrorCode errorCode;

    private Result(boolean success, T data, ErrorCode errorCode) {
        this.success = success;
        this.data = data;
        this.errorCode = errorCode;
    }

    public static <T> Result<T> ok(T data) {
        return new Result<T>(true, data, null);
    }

    public static <T> Result<T> fail(ErrorCode errorCode) {
        return new Result<T>(false, null, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}

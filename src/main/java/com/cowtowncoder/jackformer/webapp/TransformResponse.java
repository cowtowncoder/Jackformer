package com.cowtowncoder.jackformer.webapp;

public class TransformResponse<T>
{
    public final boolean ok;

    public final T transformed;

    public final ErrorType errorType;
    public final String errorMessage;

    // OK case:
    protected TransformResponse(T result)
    {
        ok = true;
        errorType = ErrorType.NONE;
        errorMessage = "";
        transformed = result;
    }

    // Fail case:
    protected TransformResponse(ErrorType et, String errorMsg)
    {
        ok = false;
        errorType = et;
        errorMessage = errorMsg;
        transformed = null;
    }

    public static TransformResponse<String> success(String content) {
        return new TransformResponse<>(content);
    }

    public static TransformResponse<byte[]> success(byte[] content) {
        return new TransformResponse<>(content);
    }

    public static <T> TransformResponse<T> validationFail(String errorMsg, Object... args) {
        return new TransformResponse<T>(ErrorType.INVALID_PARAMETER,
                String.format(errorMsg, args));
    }

    public static <T> TransformResponse<T> inputFail(String errorMsg, Object... args) {
        return new TransformResponse<T>(ErrorType.INVALID_INPUT,
                String.format(errorMsg, args));
    }

    public static <T> TransformResponse<T> transformationFail(String errorMsg, Object... args) {
        return new TransformResponse<T>(ErrorType.TRANSFORMATION_FAILED,
                String.format(errorMsg, args));
    }
}

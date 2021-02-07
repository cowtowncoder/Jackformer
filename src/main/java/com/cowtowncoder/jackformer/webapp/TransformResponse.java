package com.cowtowncoder.jackformer.webapp;

public class TransformResponse
{
    public final boolean ok;

    public final String transformed;

    public final ErrorType errorType;
    public final String errorMessage;

    // OK case:
    protected TransformResponse(String result)
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
        transformed = "";
    }
    
    public static TransformResponse success(String content) {
        return new TransformResponse(content);
    }

    public static TransformResponse validationFail(String errorTmpl, Object... args) {
        return new TransformResponse(ErrorType.INVALID_PARAMETER,
                String.format(errorTmpl, args));
    }

    public static TransformResponse inputFail(String errorTmpl, Object... args) {
        return new TransformResponse(ErrorType.INVALID_INPUT,
                String.format(errorTmpl, args));
    }
}

package com.cowtowncoder.jackformer.webapp;

public enum ErrorType
{
    // First server-side types (and 1 pseudo-type) in use

    /**
     * Failure due to invalid or incomplete parameters being sent by client
     * (system error).
     */
    INVALID_PARAMETER,

    /**
     * Failure due to content being invalid for format type specified
     * (user input error).
     */
    INVALID_INPUT,

    /**
     * Failure to transformation into output format failed
     * (typically limitation of formats).
     */
    TRANSFORMATION_FAILED,

    /**
     * No actual error; used instead of {@code null}
     */
    NONE,

    // And then client-side

    /**
     * Client-side failure (never set by server side)
     */
    SEND_FAILURE,

    /**
     * Presumed failure due to input content
     */
    INVALID_REQUEST,
    ;
}

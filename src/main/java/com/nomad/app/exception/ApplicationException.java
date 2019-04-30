package com.nomad.app.exception;

import com.nomad.app.model.EnumerationList;

/**
 * @author Md Shariful Islam
 */
public class ApplicationException extends RuntimeException {
    private int code;

    public ApplicationException(EnumerationList.ErrorHeader errorHeader, int code, Throwable e) {
        super(errorHeader.toString(), e);
        this.code = code;
    }

    public ApplicationException(EnumerationList.ErrorHeader errorHeader, int code) {
        super(errorHeader.toString());
        this.code = code;
    }

    public ApplicationException(EnumerationList.ErrorHeader errorHeader, Throwable e) {
        super(errorHeader.toString(), e);
        this.code = errorHeader.getCode();
    }

    public ApplicationException(String message, Throwable e) {
        super(message, e);
    }

    public EnumerationList.ErrorHeader getErrorHeader() {
        return EnumerationList.ErrorHeader.valueOf(this.code);
    }
}

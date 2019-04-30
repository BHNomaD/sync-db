package com.nomad.app.exception;

import com.nomad.app.model.EnumerationList;

/**
 * @author Md Shariful Islam
 */
public class ImportException extends Exception {
    private int code;

    public ImportException(EnumerationList.ErrorHeader errorHeader, Throwable e) {
        super(errorHeader.toString(), e);
        this.code = errorHeader.getCode();
    }

    public ImportException(EnumerationList.ErrorHeader errorHeader) {
        super(errorHeader.toString());
        this.code = errorHeader.getCode();
    }

    public EnumerationList.ErrorHeader getErrorHeader() {
        return EnumerationList.ErrorHeader.valueOf(this.code);
    }
}

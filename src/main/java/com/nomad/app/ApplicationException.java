package com.nomad.app;

import com.nomad.app.model.ErrorDetails;

/**
 * @author Shariful Islam
 */
public class ApplicationException extends RuntimeException {
    private ErrorDetails errorDetails;

    public ApplicationException(ErrorDetails errorDetails, String message, Throwable e) {
        super(message, e);
        this.errorDetails = errorDetails;
    }

    public ApplicationException(ErrorDetails errorDetails, String message) {
        super(message);
        this.errorDetails = errorDetails;
    }

    public ApplicationException(ErrorDetails errorDetails, Throwable e) {
        super(e);
        this.errorDetails = errorDetails;
    }

    public ApplicationException(String message, Throwable e) {
        super(message, e);
    }

    public ErrorDetails getErrorDetails() {
        return this.errorDetails;
    }
}

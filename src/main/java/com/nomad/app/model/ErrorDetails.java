package com.nomad.app.model;

/**
 * @author Shariful Islam
 */
public enum ErrorDetails {
    NONE(""),
    GENERAL_ERROR("general error"),
    PATH_NOT_EXISTS("path not exists"),
    REQUEST_ERROR("request error"),
    JASPER_ERROR("jasper error"),
    CREATE_ARCHIEVE_ERROR("create archieve error"),
    ;

    private String value;

    ErrorDetails(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
}


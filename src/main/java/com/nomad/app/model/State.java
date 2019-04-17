package com.nomad.app.model;

/**
 * @author Shariful Islam
 */
public enum State {

    CREATING_ZIP("creating_zip"),
    CLEANING_TEMPORARY_FILE("cleaning_temporary_file")
    ;

    private String value;

    State(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

}

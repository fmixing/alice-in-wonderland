package com.test.clientclasses;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultID {

    Long jsonID;

    String message;

    public boolean hasMessage() {
        if (message != null)
            return true;
        return false;
    }

    public boolean hasResult() {
        if (jsonID != null)
            return true;
        return false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getJsonID() {
        return jsonID;
    }

    public void setJsonID(Long jsonID) {
        this.jsonID = jsonID;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}

package com.alice.utils;

import java.util.Optional;

public class Result<T> {

    /**
     * Contains the instance of T class if the result can be received
     */
    private T result;

    /**
     * Contains the exception message
     */
    private String message;

    /**
     * True if the result is present, false otherwise
     */
    private boolean isPresent = false;

    /**
     * True if the error message was written
     */
    private boolean hasMessage = false;

    public void setResult(T result) {
        this.result = result;
        isPresent = true;
    }

    public void setMessage(String message) {
        this.message = message;
        hasMessage = true;
    }

    public boolean isPresent() {
        return isPresent;
    }

    public boolean hasMessage() {
        return hasMessage;
    }

    public T getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}

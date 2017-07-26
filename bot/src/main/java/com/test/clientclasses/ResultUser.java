package com.test.clientclasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResultUser {

    User jsonUser;

    String message;

    public boolean hasMessage() {
        if (message != null)
            return true;
        return false;
    }

    public boolean hasResult() {
        if (jsonUser != null)
            return true;
        return false;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getJsonUser() {
        return jsonUser;
    }

    public void setJsonUser(User jsonUser) {
        this.jsonUser = jsonUser;
    }

    @Override
    public String toString() {
        return "Result{" +
                "jsonUser=" + jsonUser +
                '}';
    }
}

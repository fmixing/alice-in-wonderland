package com.alice.dao;


import java.util.Optional;


/**
 * @author nkalugin on 06.07.17.
 */
public interface Result<T>
{
    void setResult(T result);

    boolean hasError();

    String getError();

    Optional<T> getResult();

    void setError(String error);
}

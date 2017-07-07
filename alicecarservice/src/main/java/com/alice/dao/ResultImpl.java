package com.alice.dao;


import java.util.Optional;


/**
 * @author nkalugin on 06.07.17.
 */
public class ResultImpl<T> implements Result<T>
{
    private T result;

    private String error;

    @Override
    public void setResult(T result)
    {
        this.result = result;
    }


    @Override
    public boolean hasError()
    {
        return error != null;
    }


    @Override
    public String getError()
    {
        return error;
    }


    @Override
    public Optional<T> getResult()
    {
        if (!hasError())
        {
            return Optional.ofNullable(result);
        }
        return Optional.empty();
    }
}

package com.alice.dao;


import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * @author nkalugin on 06.07.17.
 */
public abstract class AbstractDAO<View, T extends Lock> implements DAO<View, T>
{
    @Override
    public <RT> Result<RT> modifyWithResult(long ID, BiFunction<Result<RT>, T, Optional<T>> modifier)
    {
        Result<RT> result = new ResultImpl<>();
        get(ID).flatMap(value -> {
            value.lock();
            try {
                return modifier.apply(result, value)
                        .map(v -> put(ID, v));
            }
            finally
            {
                value.unlock();
            }
        });
        return result;
    }


    @Override
    public Optional<View> modify(long ID, Function<T, Optional<T>> modifier)
    {
        return get(ID).flatMap(value -> {
            value.lock();
            try {
                return modifier.apply(value)
                        .map(v -> put(ID, value));
            }
            finally
            {
                value.unlock();
            }
        });
    }


    protected abstract Optional<T> get(long ID);

    protected abstract View put(long ID, T value);
}

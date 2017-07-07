package com.alice.dao;


import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * @author nkalugin on 06.07.17.
 */
public interface DAO<View, T extends Lock>
{
    Optional<View> getViewByID(long ID);

    <RT> Result<RT> modifyWithResult(long ID, BiFunction<Result<RT>, T, Optional<T>> modifier);

    Optional<View> modify(long ID, Function<T, Optional<T>> modifier);

    Collection<View> getAllViews();
}

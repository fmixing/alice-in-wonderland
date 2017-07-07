package com.alice.dbclasses.user;

import com.alice.dao.AbstractDAO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserDAOImpl extends AbstractDAO<UserView, User>
{
    /**
     * Maps users IDs to {@code User}
     */
    private final Map<Long, User> users;

    public UserDAOImpl() {
        users = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<UserView> getViewByID(long ID)
    {
        return Optional.ofNullable(users.get(ID));
    }


    @Override
    public Collection<UserView> getAllViews()
    {
        return Collections.unmodifiableCollection(users.values());
    }


    @Override
    protected Optional<User> get(long ID)
    {
        return Optional.ofNullable(users.get(ID));
    }


    @Override
    protected UserView put(long ID, User value)
    {
        Objects.requireNonNull(value);
        users.put(value.getUserID(), value);
        return value;
    }
}

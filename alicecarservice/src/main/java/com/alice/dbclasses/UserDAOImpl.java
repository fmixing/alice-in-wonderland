package com.alice.dbclasses;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserDAOImpl implements UserDAO{

    /**
     * Maps users IDs to {@code User}
     */
    private Map<Long, User> users;

    public UserDAOImpl() {
        users = new ConcurrentHashMap<>();
    }

    /**
     * @param ID user's ID
     * @return a User object if the user with this ID exists, null otherwise
     */
    @Override
    public User getUserByID(long ID) {
        return users.get(ID);
    }

    /**
     * @param user which is needed to be in DB
     * @return a User object which now contains in DB
     */
    @Override
    public User putUser(User user) {
        users.put(user.getUserID(), user);
        return user;
    }

    /**
     * @return a list of all created users
     */
    @Override
    public List<User> getUsers() {
        return new ArrayList<>(users.values());
    }
}

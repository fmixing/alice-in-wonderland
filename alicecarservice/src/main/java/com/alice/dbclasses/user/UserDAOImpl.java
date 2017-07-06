package com.alice.dbclasses.user;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class UserDAOImpl implements UserDAO{

    /**
     * Maps users IDs to {@code User}
     */
    private final Map<Long, User> users;

    public UserDAOImpl() {
        users = new ConcurrentHashMap<>();
    }

    /**
     * @param ID user's ID
     * @return an {@code Optional} object which contains cloned {@code User} if a user with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<User> getUserByID(long ID) {
        return Optional.ofNullable(users.get(ID)).map(User::cloneUser);
    }

    /**
     * @param user which is needed to be in DB
     * @return a User object which copy now contains in DB
     */
    @Override
    public User putUser(User user) {
        // тут потенциальное NPE
        Objects.requireNonNull(user);
        users.put(user.getUserID(), user.cloneUser());
        return user;
    }

    /**
     * @return a list of previews of all created users
     */
    @Override
    public List<UserView> getUsers() {
        return new ArrayList<>(users.values());
    }
}

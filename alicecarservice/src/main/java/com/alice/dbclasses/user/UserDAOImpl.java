package com.alice.dbclasses.user;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Component
public class UserDAOImpl implements UserDAO {

    /**
     * Maps users IDs to {@code User}
     */
    private final Map<Long, User> users;

    public UserDAOImpl() {
        users = new ConcurrentHashMap<>();
    }

    /**
     * @param ID user's ID
     * @return an {@code Optional} object which {@code User} if a user with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<UserView> getUserByID(long ID) {
        return Optional.ofNullable(users.get(ID));
    }

    /**
     * @param ID of the user which is needed to be modified
     * @param mapper a function that somehow modifies the user
     * @return a modified user
     */
    @Override
    public Optional<UserView> modify(long ID, Function<User, Optional<User>> mapper) {
        return Optional.ofNullable(users.get(ID)).flatMap(user -> {
            user.lock();
            try {
                return mapper.apply(user).map(result -> users.put(ID, result));
            } finally {
                user.unlock();
            }
        });
    }

    /**
     * @param ID new user's ID
     * @return a preview of created user
     */
    @Override
    public UserView createUser(long ID) {
        User user = new User(ID);
        users.put(ID, user);
        return user;
    }

    /**
     * @return a Collection of previews of all created users
     */
    @Override
    public Collection<UserView> getUsers() {
        return Collections.unmodifiableCollection(users.values());
    }
}

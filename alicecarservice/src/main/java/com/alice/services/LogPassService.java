package com.alice.services;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LogPassService {

    /**
     * Maps user's login to {@code LoginInfo} which contains user's password and ID
     */
    private final Map<String, LoginInfo> loginInfoMap = new ConcurrentHashMap<>();

    /**
     * Creates a new unique ID
     */
    private AtomicLong id = new AtomicLong(0);


    /**
     * Creates a record about new user's data
     * @param login a unique string which is needed to map user login to ID
     * @param password a password string
     * @return created user's ID, -1 if a user with this login already exists
     */
    public Optional<Long> createNewUser(String login, String password) {
        if (loginInfoMap.putIfAbsent(login, new LoginInfo(password, id.incrementAndGet())) != null)
            return Optional.empty();

        return Optional.of(loginInfoMap.get(login).ID);
    }

    /**
     * @param login a login string
     * @param password a password string
     * @return user's ID, -1 if a user with this data doesn't exist
     */
    public Optional<Long> getUserID(String login, String password) {
        if (!loginInfoMap.containsKey(login) || !loginInfoMap.get(login).password.equals(password)) {
            return Optional.empty();
        }
        return Optional.of(loginInfoMap.get(login).ID);
    }

    /**
     * Class which contains user's password and ID
     */
    private static class LoginInfo {
        private final long ID;
        private final String password;

        LoginInfo(String password, long ID) {
            this.ID = ID;
            this.password = password;
        }
    }
}

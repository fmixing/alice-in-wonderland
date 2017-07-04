package com.alice.dbclasses;


import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class LogPassService {

    /**
     * Maps user's login to user's password
     */
    private final Map<String, String> logToPass;

    /**
     * Mas user's login to user's ID
     */
    private final Map<String, Long> logToID;

    /**
     * Creates a new unique ID
     */
    private AtomicLong id;

    LogPassService() {
        logToPass = new ConcurrentHashMap<>();
        logToID = new ConcurrentHashMap<>();
        id = new AtomicLong(0);
    }

    /**
     * Creates a record about new user's data
     * @param login a unique string which is needed to map user login to ID
     * @param password a password string
     * @return created user's ID, -1 if a user with this login already exists
     */
    public long createNewUser(String login, String password) {
        synchronized (logToPass) {
            if (logToPass.containsKey(login)) {
                return -1;
            }
            logToPass.put(login, password);
        }
        logToID.put(login, id.incrementAndGet());

        return logToID.get(login);
    }

    /**
     * @param login a login string
     * @param password a password string
     * @return user's ID, -1 if a user with this data doesn't exist
     */
    public long getUserID(String login, String password) {
        if (!logToPass.containsKey(login) || !logToPass.get(login).equals(password)) {
            return -1;
        }
        return logToID.get(login);
    }

}

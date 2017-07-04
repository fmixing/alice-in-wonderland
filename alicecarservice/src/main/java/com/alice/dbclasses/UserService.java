package com.alice.dbclasses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;


/**
 * TODO: exceptions?
 */

@Component
public class UserService {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LogPassService logPassService;

    /**
     * @return created user with this personal data, null if a user with this login already exists
     */
    public User addUser(String login, String password) {
        long ID = logPassService.createNewUser(login, password);
        if (ID == -1) {
            return null;
        }
        User user = new User(ID);
        return userDAO.putUser(user);
    }

    /**
     * @param ID user's ID
     * @return a user with this ID, null if a users with this doesn't exists
     */
    public User getUser(long ID) {
        return userDAO.getUserByID(ID);
    }

    /**
     * @return all the created users
     */
    public List<User> getAllUsers() {
        return userDAO.getUsers();
    }
}

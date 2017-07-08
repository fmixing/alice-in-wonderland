package com.alice.services;

import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Component
public class UserService {

    private final UserDAO userDAO;

    private final LogPassService logPassService;

    @Autowired
    public UserService(UserDAO userDAO, LogPassService logPassService) {
        this.userDAO = userDAO;
        this.logPassService = logPassService;
    }

    /**
     * @return {@code Result} object contains created user with this personal data,
     * error message if a user with this login already exists
     */
    public Result<UserView> addUser(String login, String password) {
        Optional<Long> ID = logPassService.createNewUser(login, password);

        Result<UserView> result = new Result<>();

        if (!ID.isPresent()) {
            result.setMessage("User with login " + login + " already exists");
            return result;
        }

        result.setResult(userDAO.createUser(ID.get()));
        return result;
    }

    /**
     * @param ID user ID
     * @return a {@code Result} object contains userView with this ID,
     * error message if a user with this ID doesn't exist
     */
    public Result<UserView> getUser(long ID) {
        Optional<UserView> user = userDAO.getUserByID(ID);

        Result<UserView> result = new Result<>();

        if (!user.isPresent()) {
            result.setMessage("User with ID " + ID + " doesn't exist");
            return result;
        }

        result.setResult(user.get());

        return result;
    }

    /**
     * @return all the created users
     */
    public Collection<UserView> getAllUsers() {
        return userDAO.getUsers();
    }
}

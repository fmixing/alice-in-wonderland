package com.alice.Services;

import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
     * @return created user with this personal data, null if a user with this login already exists
     */
    public User addUser(String login, String password) {
        Optional<Long> ID = logPassService.createNewUser(login, password);

        if (!ID.isPresent()) {
            return null;
        }

        User user = new User(ID.get());
        return userDAO.putUser(user);
    }

    /**
     * @param ID user ID
     * @return a user with this ID, null if a user with this ID doesn't exist
     */
    public User getUser(long ID) {
        Optional<User> user = userDAO.getUserByID(ID);
      //  Optional<Long> maybeUserId = user.map(User::getUserID);
        return user.orElse(null);
    }

    /**
     * @return all the created users
     */
    public List<UserView> getAllUsers() {
        return userDAO.getUsers();
    }
}

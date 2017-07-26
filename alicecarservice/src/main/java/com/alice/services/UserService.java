package com.alice.services;

import com.alice.dbclasses.UpdateDB;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.Result;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

@Component
public class UserService {

    private final UserDAO userDAO;

    private final LogPassService logPassService;

    private final UpdateDB updateDB;

    private MetricRegistry registry;

    @Autowired
    public UserService(UserDAO userDAO, LogPassService logPassService, UpdateDB updateDB) {
        this.userDAO = userDAO;
        this.logPassService = logPassService;
        this.updateDB = updateDB;
        registry = new MetricRegistry();
    }

    /**
     * @return {@code Result} object contains created user with this personal data,
     * error message if a user with this login already exists
     */
    public Result<UserView> addUser(String login, String password) throws DataAccessException {

        final Timer timer = registry.timer(name(UserService.class, "addUser-request"));

        final Timer.Context context = timer.time();
        try {
            Optional<Long> ID = logPassService.getIDForUser(login, password);

            Result<UserView> result = new Result<>();

            if (!ID.isPresent()) {
                result.setMessage("User with login " + login + " already exists");
                return result;
            }

            userDAO.createUser(ID.get(), user -> {
                updateDB.updateUserLogPass(user, ID.get(), login, password);
                userDAO.putToCache(user);
                result.setResult(user);
            });

            return result;
        } finally {
            context.stop();
        }


    }

    /**
     * @param ID user ID
     * @return a {@code Result} object contains userView with this ID,
     * error message if a user with this ID doesn't exist
     */
    public Result<UserView> getUser(long ID) {
        final Timer timer = registry.timer(name(UserService.class, "getUser-request"));

        final Timer.Context context = timer.time();

        try {
            Optional<UserView> user = userDAO.getUserByID(ID);

            Result<UserView> result = new Result<>();

            if (!user.isPresent()) {
                result.setMessage("User with ID " + ID + " doesn't exist");
                return result;
            }

            result.setResult(user.get());

            return result;
        } finally {
            context.stop();
        }
    }

    /**
     * @return all the created users
     */
    public Collection<UserView> getAllUsers() {
        final Timer timer = registry.timer(name(UserService.class, "getUser-request"));

        final Timer.Context context = timer.time();
        try {
            return userDAO.getUsers();
        } finally {
            context.stop();
        }
    }
}

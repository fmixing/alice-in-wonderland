package com.alice.services;

import com.alice.dbclasses.UpdateDB;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.CommonMetrics;
import com.alice.utils.Result;
import com.codahale.metrics.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

@Component
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    private final UserDAO userDAO;

    private final LogPassService logPassService;

    private final UpdateDB updateDB;

    @Autowired
    public UserService(UserDAO userDAO, LogPassService logPassService, UpdateDB updateDB) {
        this.userDAO = userDAO;
        this.logPassService = logPassService;
        this.updateDB = updateDB;
    }

    /**
     * @return {@code Result} object contains created user with this personal data,
     * error message if a user with this login already exists
     */
    public Result<UserView> addUser(String login, String password) throws DataAccessException {

        final Timer.Context context = CommonMetrics.getTimerContext(UserService.class, "addUser-request");
        final Timer.Context contextAll = CommonMetrics.getTimerContext(UserService.class,"allOps-request");
        try {
            Optional<Long> ID = logPassService.getIDForUser(login, password);

            Result<UserView> result = new Result<>();

            if (!ID.isPresent()) {
                result.setMessage("User with login " + login + " already exists");
                return result;
            }

            userDAO.createUser(ID.get(), user -> {
                final Timer.Context contextUpdateUser = CommonMetrics.getTimerContext(UserService.class, "updateUser-request");
                try {
                    updateDB.updateUserLogPass(user, ID.get(), login, password);
                } finally {
                    contextUpdateUser.stop();
                }
                userDAO.putToCache(user);
                result.setResult(user);
            });

            return result;
        } finally {
            context.stop();
            contextAll.stop();
        }


    }

    /**
     * @param ID user ID
     * @return a {@code Result} object contains userView with this ID,
     * error message if a user with this ID doesn't exist
     */
    public Result<UserView> getUser(long ID) {

        final Timer.Context context = CommonMetrics.getTimerContext(UserService.class, "getUser-request");
        final Timer.Context contextAll = CommonMetrics.getTimerContext(UserService.class,"allOps-request");

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
            contextAll.stop();
        }
    }

    /**
     * @return all the created users
     */
    public Collection<UserView> getAllUsers() {

        final Timer.Context context = CommonMetrics.getTimerContext(UserService.class,"getAllUsers-request");

        try {
            return userDAO.getUsers();
        } finally {
            context.stop();
        }
    }
}

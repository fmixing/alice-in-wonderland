package com.alice.services;


import com.alice.utils.CommonMetrics;
import com.codahale.metrics.Timer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;


import java.util.Optional;

@Component
public class LogPassService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Creates a unique ID for user
     * @param login a unique string which is needed to map user login to ID
     * @param password a password string
     * @return Optional object contains user ID if the user with this data exists,
     * Optional.empty otherwise
     */
    public Optional<Long> getIDForUser(String login, String password) {
        Long userID;
        try {
            jdbcTemplate.queryForObject("select id from logpass where log = ?", Long.class, login);
        } catch (EmptyResultDataAccessException e) {
            userID = jdbcTemplate.queryForObject("select nextval('users_ids')", Long.class);

            return Optional.ofNullable(userID);
        }

        return Optional.empty();

    }

    /**
     * @param login a login string
     * @param password a password string
     * @return Optional object contains user ID if the user with this data exists,
     * Optional.empty otherwise
     */
    public Optional<Long> getUserID(String login, String password) {
        final Timer.Context context = CommonMetrics.getTimerContext(LogPassService.class,"getUserByLogPass-request");

        try {
            String got_pass;
            Long got_id;
            try {
                got_pass = jdbcTemplate.queryForObject("select pass from logpass where log = ?", String.class, login);
                got_id = jdbcTemplate.queryForObject("select id from logpass where log = ?", Long.class, login);
            } catch (EmptyResultDataAccessException e) {
                return Optional.empty();
            }

            if (!got_pass.equals(password)) {
                return Optional.empty();
            }
            return Optional.of(got_id);
        } finally {
            context.stop();
        }
    }

}

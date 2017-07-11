package com.alice.services;


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
    public Optional<Long> createNewUser(String login, String password) {
        Long userID;
        try {
            jdbcTemplate.queryForObject("select id from logpass where log like ?", Long.class, login);
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
        String got_pass;
        Long got_id;
        try {
            got_pass = jdbcTemplate.queryForObject("select pass from logpass where log like ?", String.class, login);
            got_id = jdbcTemplate.queryForObject("select id from logpass where log like ?", Long.class, login);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }

        if (got_pass.equals(password)) {
            return Optional.empty();
        }
        return Optional.of(got_id);
    }

}

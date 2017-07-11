package com.alice.dbclasses.user;

import com.google.common.base.Throwables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import javax.sql.rowset.serial.SerialJavaObject;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Component
public class UserDAOImpl implements UserDAO {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * Maps users IDs to {@code User}
     */
    private final Map<Long, User> users;

    public UserDAOImpl() {
        users = new ConcurrentHashMap<>();
    }


//    @Override
//    public Optional<UserView> getUserByID(long ID) {
//        return Optional.ofNullable(users.get(ID));
//    }

    /**
     * @param ID user's ID
     * @return an {@code Optional} object which {@code User} if a user with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<UserView> getUserByID(long ID)
    {
        return getUser(ID).map(value -> (UserView) value);
    }

    private Optional<User> getUser(long ID) {
        User user;
        try {
            user = users.computeIfAbsent(ID, userID ->
                (User) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from users where id = ?",
                    byte[].class, userID)
//                    jdbcTemplate.queryForObject("select * from users where id = ?",
//                            (resultSet, i) -> (User) resultSet.getObject(2),
//                            userID
                    ));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return Optional.of(user);
    }


    /**
     * @param ID of the user which is needed to be modified
     * @param mapper a function that somehow modifies the user
     * @return a modified user
     */
    @Override
    public Optional<UserView> modify(long ID, Function<User, Optional<User>> mapper) {
        return getUser(ID).flatMap(user -> {
            user.lock();
            try {
                return mapper.apply(user).map(result -> users.put(ID, result));
            }
            catch (Exception e)
            {
                // logger warn
                users.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
                user.unlock();
            }
        });
    }

    /**
     * @param ID new user's ID
     * @return a preview of created user
     */
    @Override
    public User createUser(long ID) {
        return new User(ID);
    }

    @Override
    public void putToCache(User user) {
        users.putIfAbsent(user.getUserID(), user);
    }

    /**
     * @return a Collection of previews of all created users
     */
    @Override
    public Collection<UserView> getUsers() {
        List<byte[]> usersByteList = jdbcTemplate.queryForList("select blob from users", byte[].class);

        Collection<UserView> allUsers = new ArrayList<>();

        usersByteList.forEach(v -> allUsers.add((User) SerializationUtils.deserialize(v)));

      //  return Collections.unmodifiableCollection(users.values());
        return allUsers;
    }

}

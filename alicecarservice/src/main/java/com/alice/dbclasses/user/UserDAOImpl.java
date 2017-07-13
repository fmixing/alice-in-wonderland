package com.alice.dbclasses.user;

import com.google.common.base.Throwables;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

@Component
public class UserDAOImpl implements UserDAO {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(UserDAOImpl.class);


    /**
     * Maps users IDs to {@code User}
     */
    //private final Map<Long, User> users;

    private final Ehcache usersCache;

    @Autowired
    public UserDAOImpl(CacheManager cacheManager, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
//        users = new ConcurrentHashMap<>();
        usersCache = cacheManager.getCache("usersCache");
        Objects.requireNonNull(usersCache);
    }

    /**
     * @param ID user's ID
     * @return an {@code Optional} object which {@code User} if a user with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<UserView> getUserByID(long ID)
    {
        usersCache.acquireReadLockOnKey(ID);
        try {
            return getUser(ID).map(value -> (UserView) value);
        } finally {
            usersCache.releaseReadLockOnKey(ID);
        }
    }

    private Optional<User> getUser(long ID) {
        User user;
        try {
            if (usersCache.isKeyInCache(ID))
                return Optional.of((User) usersCache.get(ID).getObjectValue());

            user = (User) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from users where id = ?",
                    byte[].class, ID));

            usersCache.put(new Element(ID, user));
//            user = users.computeIfAbsent(ID, userID ->
//                (User) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from users where id = ?",
//                    byte[].class, userID)
//                    ));
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
        usersCache.acquireWriteLockOnKey(ID);
        return getUser(ID).flatMap(user -> {
        //    user.lock();
            try {
                Optional<User> result = mapper.apply(user);
                if (!result.isPresent())
                    return Optional.empty();
                usersCache.put(new Element(ID, result.get()));
                return Optional.of(result.get());
//                mapper.apply(user)
//                         .map(result -> usersCache.put(new Element(ID, result)));
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while doing modify on user with ID " + ID);
//                users.remove(ID);
                usersCache.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
//                user.unlock();
                usersCache.releaseWriteLockOnKey(ID);
            }
        });
    }

    /**
     * @param ID new user's ID
     * @return a preview of created user
     */
    @Override
    public UserView createUser(long ID, Consumer<User> mapper) {
        User user = new User(ID);

        try {
            mapper.accept(user);
        }
        catch (Exception e)
        {
            logger.error("Failed to access database while creating user with ID " + ID);
            throw Throwables.propagate(e);
        }

        return user;
    }

    @Override
    public void putToCache(User user) {
       // users.putIfAbsent(user.getUserID(), user);
        usersCache.putIfAbsent(new Element(user.getUserID(), user));
    }

    /**
     * @return a Collection of previews of all created users
     */
    @Override
    public Collection<UserView> getUsers() {
        List<byte[]> usersByteList = jdbcTemplate.queryForList("select blob from users", byte[].class);

        Collection<UserView> allUsers = new ArrayList<>();

        usersByteList.forEach(v -> allUsers.add((User) SerializationUtils.deserialize(v)));

        return allUsers;
    }

}

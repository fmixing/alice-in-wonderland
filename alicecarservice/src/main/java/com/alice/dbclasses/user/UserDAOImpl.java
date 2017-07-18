package com.alice.dbclasses.user;

import com.google.common.base.Throwables;
import net.sf.ehcache.*;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
    private final SelfPopulatingCache selfPopulatingCache;

    private final Ehcache usersLockCache;

    @Autowired
    public UserDAOImpl(CacheManager cacheManager, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        Ehcache usersCache = cacheManager.getCache("usersCache");
        Objects.requireNonNull(usersCache);
        selfPopulatingCache = new SelfPopulatingCache(usersCache, key ->
                SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from users where id = ?",
                    byte[].class, (Long) key)));
        usersLockCache = cacheManager.getCache("usersLockCache");
        Objects.requireNonNull(usersLockCache);
    }

    /**
     * @param ID user's ID
     * @return an Optional object contains cloned {@code User} if a user with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<UserView> getUserByID(long ID)
    {
        usersLockCache.acquireReadLockOnKey(ID);
        try {
            return getUser(ID).map(value -> (UserView) org.apache.commons.lang3.SerializationUtils.clone(value));
        } finally {
            usersLockCache.releaseReadLockOnKey(ID);
        }
    }

    /**
     * Gets user from cache if it is possible, if not try to get it from database
     * @return an Optional object contains {@code User} if a user with this ID exists,
     * an empty optional otherwise
     */
    private Optional<User> getUser(long ID) {
        try {
            Element element = selfPopulatingCache.get(ID);
            Objects.requireNonNull(element);
            return Optional.of((User) element.getObjectValue());
        } catch (EmptyResultDataAccessException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * @param ID of the user which is needed to be modified
     * @param mapper a function that somehow modifies the user
     * @return a modified user
     */
    @Override
    public Optional<UserView> modify(long ID, Function<User, Optional<User>> mapper) {
        usersLockCache.acquireWriteLockOnKey(ID);
        return getUser(ID).flatMap(user -> {
            try {
                return mapper.apply(user)
                         .map(result -> {
                             selfPopulatingCache.put(new Element(ID, result));
                             return result;
                         });
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while doing modify on user with ID " + ID);
                selfPopulatingCache.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
                usersLockCache.releaseWriteLockOnKey(ID);
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

    /**
     * Puts a user to cache if it is absent there, using after creating user
     */
    @Override
    public void putToCache(User user) {
        selfPopulatingCache.putIfAbsent(new Element(user.getUserID(), user));
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

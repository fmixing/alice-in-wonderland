package com.alice.dbclasses.drive;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Throwables;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.SerializationUtils;

import java.util.*;

@Component
public class DriveDAOImpl implements DriveDAO {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DriveDAOImpl.class);

    /**
     * Maps users IDs to {@code User}
     */
    private final SelfPopulatingCache selfPopulatingCache;

    private final Ehcache drivesLockCache;


    @Autowired
    public DriveDAOImpl(CacheManager cacheManager, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        Ehcache driversCache = cacheManager.getCache("drivesCache");
        Objects.requireNonNull(driversCache);
        selfPopulatingCache = new SelfPopulatingCache(driversCache, key ->
                SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from drives where id = ?",
                        byte[].class, (Long) key)));
        drivesLockCache = Objects.requireNonNull(cacheManager.getCache("drivesLockCache"));
    }


    /**
     * @return a preview of created drive
     */
    @Override
    public DriveView createDrive(long userID, long from, long to, long date, int vacantPlaces, Consumer<Drive> mapper) {
        Long driveID = jdbcTemplate.queryForObject("select nextval('drives_ids')", Long.class);

        logger.error("Try to get write lock on driveID {}", driveID);
        drivesLockCache.acquireWriteLockOnKey(driveID);
        logger.error("Got write lock on driveID {}", driveID);
        try {
            Drive drive = new Drive(driveID, userID, from, to, date, vacantPlaces);

            try {
                mapper.accept(drive);
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while creating drive with ID {} and user ID {}", driveID, userID);
                throw Throwables.propagate(e);
            }

            return drive;
        } finally {
            drivesLockCache.releaseWriteLockOnKey(driveID);
            logger.error("Released write lock on driveID {}", driveID);
        }
    }

    /**
     * @param ID drive's ID
     * @return an Optional object which contains cloned {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<DriveView> getDriveByID(long ID)
    {
        logger.error("Try to get read lock on driveID {}", ID);
        drivesLockCache.acquireReadLockOnKey(ID);
        logger.error("Got read lock on driveID {}", ID);

        try {
            return getDrive(ID).map(value -> (DriveView) org.apache.commons.lang3.SerializationUtils.clone(value));
        } finally {
            drivesLockCache.releaseReadLockOnKey(ID);
            logger.error("Released read lock on driveID {}", ID);
        }
    }

    /**
     * Gets drive from cache if it is possible, if not try to get it from database
     * @return an Optional object which contains {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    private Optional<Drive> getDrive(long ID) {
        try {
            Element element = selfPopulatingCache.get(ID);
            Objects.requireNonNull(element);

            return Optional.of((Drive) element.getObjectValue());

        } catch (EmptyResultDataAccessException | NullPointerException e) {
            return Optional.empty();
        }
    }


    /**
     * @param ID of the drive which is needed to be modified
     * @param mapper a function that somehow modifies the drive
     * @return a modified drive
     */
    @Override
    public Optional<DriveView> modify(long ID, Function<Drive, Optional<Drive>> mapper) {
        logger.error("Try to get write lock on driveID {}", ID);
        drivesLockCache.acquireWriteLockOnKey(ID);
        logger.error("Got write lock on driveID {}", ID);
        return getDrive(ID).flatMap(drive -> {
            try {
                return mapper.apply(drive)
                        .map(result -> {
                            selfPopulatingCache.put(new Element(ID, result));
                            return result;
                        });
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while doing modify on drive with ID {}", ID);
                selfPopulatingCache.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
                drivesLockCache.releaseWriteLockOnKey(ID);
                logger.error("Released write lock on driveID {}", ID);
            }
        });
    }

    /**
     * Puts a drive to cache if it is absent there, using after creating drive
     */
    @Override
    public void putToCache(Drive drive) {
        selfPopulatingCache.putIfAbsent(new Element(drive.getDriveID(), drive));
    }


    /**
     * @return a Collection of previews of all created drives
     */
    @Override
    public Collection<DriveView> getDrives() {
        List<byte[]> drivesByteList = jdbcTemplate.queryForList("select blob from drives", byte[].class);

        Collection<DriveView> allDrives = new ArrayList<>();

        drivesByteList.forEach(v -> allDrives.add((Drive) SerializationUtils.deserialize(v)));

        return allDrives;

    }

}

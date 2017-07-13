package com.alice.dbclasses.drive;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Throwables;
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

@Component
public class DriveDAOImpl implements DriveDAO {

    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DriveDAOImpl.class);

    /**
     * Maps drives IDs to {@code Drive}
     */
    private final Ehcache driversCache;


    @Autowired
    public DriveDAOImpl(CacheManager cacheManager, JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        driversCache = cacheManager.getCache("drivesCache");
        Objects.requireNonNull(driversCache);
    }


    /**
     * @return a preview of created drive
     */
    @Override
    public DriveView createDrive(long userID, long from, long to, long date, int vacantPlaces, Consumer<Drive> mapper) {
        Long driveID = jdbcTemplate.queryForObject("select nextval('drives_ids')", Long.class);

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
    }

    /**
     * @param ID drive's ID
     * @return an Optional object which contains cloned {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<DriveView> getDriveByID(long ID)
    {
        driversCache.acquireReadLockOnKey(ID);
        try {
            return getDrive(ID).map(value -> (DriveView) org.apache.commons.lang3.SerializationUtils.clone(value));
        } finally {
            driversCache.releaseReadLockOnKey(ID);
        }
    }

    /**
     * Gets drive from cache if it is possible, if not try to get it from database
     * @return an Optional object which contains {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    private Optional<Drive> getDrive(long ID) {
        Drive drive;
        try {
            Element element = driversCache.get(ID);
            if (element != null)
                return Optional.of((Drive) element.getObjectValue());

            drive = (Drive) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from drives where id = ?",
                        byte[].class, ID));

            driversCache.put(new Element(ID, drive));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
        return Optional.of(drive);
    }


    /**
     * @param ID of the drive which is needed to be modified
     * @param mapper a function that somehow modifies the drive
     * @return a modified drive
     */
    @Override
    public Optional<DriveView> modify(long ID, Function<Drive, Optional<Drive>> mapper) {
        driversCache.acquireWriteLockOnKey(ID);
        return getDrive(ID).flatMap(drive -> {
            try {
                return mapper.apply(drive).map(result -> {
                    driversCache.put(new Element(ID, result));
                    return result;
                });
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while doing modify on drive with ID {}", ID);
                driversCache.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
                driversCache.releaseWriteLockOnKey(ID);
            }
        });
    }

    /**
     * Puts a drive to cache if it is absent there, using after creating drive
     */
    @Override
    public void putToCache(Drive drive) {
        driversCache.putIfAbsent(new Element(drive.getDriveID(), drive));
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

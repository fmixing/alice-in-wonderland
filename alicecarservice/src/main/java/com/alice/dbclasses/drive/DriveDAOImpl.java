package com.alice.dbclasses.drive;

import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.base.Throwables;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
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

@Component
public class DriveDAOImpl implements DriveDAO {

    private final CacheManager cacheManager;

    private final JdbcTemplate jdbcTemplate;

    private static final Logger logger = LoggerFactory.getLogger(DriveDAOImpl.class);


    /**
     * Maps drives IDs to {@code Drive}
     */
//    private final Map<Long, Drive> drives;

    private final Cache driversCache;


    @Autowired
    public DriveDAOImpl(CacheManager cacheManager, JdbcTemplate jdbcTemplate) {
//        drives = new ConcurrentHashMap<>();
        this.cacheManager = cacheManager;
        this.jdbcTemplate = jdbcTemplate;
//        users = new ConcurrentHashMap<>();
        driversCache = this.cacheManager.getCache("driversCache");
    }


    /**
     * @return created drive
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
            logger.error("Failed to access database while creating drive with ID " + driveID + " and user ID " + userID);
            throw Throwables.propagate(e);
        }

        return drive;
    }

    /**
     * @param ID drive's ID
     * @return an {@code Optional} object which contains {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<DriveView> getDriveByID(long ID)
    {
        driversCache.acquireReadLockOnKey(ID);
        try {
            return getDrive(ID).map(value -> (DriveView) value);
        } finally {
            driversCache.releaseReadLockOnKey(ID);
        }
    }

    private Optional<Drive> getDrive(long ID) {
        Drive drive;
        try {
            if (driversCache.isKeyInCache(ID))
                return Optional.of((Drive) driversCache.get(ID).getObjectValue());

            drive = (Drive) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from drives where id = ?",
                        byte[].class, ID));

            driversCache.put(new Element(ID, drive));
//            drive = drives.computeIfAbsent(ID, driveID ->
//                    (Drive) SerializationUtils.deserialize(jdbcTemplate.queryForObject("select blob from drives where id = ?",
//                        byte[].class, driveID))
//            );
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
//            drive.lock();
            try {
                Optional<Drive> result = mapper.apply(drive);
                if (!result.isPresent())
                    return Optional.empty();
                driversCache.put(new Element(ID, result.get()));
                return Optional.of(result.get());
//                return mapper.apply(drive).map(result -> drives.put(ID, result));
            }
            catch (Exception e)
            {
                logger.error("Failed to access database while doing modify on drive with ID " + ID);
//                drives.remove(ID);
                driversCache.remove(ID);
                throw Throwables.propagate(e);
            }
            finally {
//                drive.unlock();
                driversCache.releaseWriteLockOnKey(ID);
            }
        });
    }

    @Override
    public void putToCache(Drive drive) {
//        drives.putIfAbsent(drive.getDriveID(), drive);
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

package com.alice.dbclasses.drive;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DriveDAOImpl implements DriveDAO {

    /**
     * Maps drives IDs to {@code Drive}
     */
    private final Map<Long, Drive> drives;

    private final AtomicLong id;

    public DriveDAOImpl() {
        drives = new ConcurrentHashMap<>();
        id = new AtomicLong(0);
    }

    /**
     * @return a preview of the created drive
     */
    @Override
    public DriveView createDrive(long userID, long from, long to, long date, int vacantPlaces) {
        long driveID = id.incrementAndGet();
        Drive drive = new Drive(driveID, userID, from, to, date, vacantPlaces);
        drives.put(driveID, drive);
        return drive;
    }

    /**
     * @param ID drive's ID
     * @return an {@code Optional} object which contains {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<DriveView> getDriveByID(long ID) {
        return Optional.ofNullable(drives.get(ID));
    }


    /**
     * @param ID of the drive which is needed to be modified
     * @param mapper a function that somehow modifies the drive
     * @return a modified drive
     */
    @Override
    public Optional<DriveView> modify(long ID, Function<Drive, Optional<Drive>> mapper) {

        return Optional.ofNullable(drives.get(ID)).flatMap(drive -> {
            drive.lock();
            try {
                return mapper.apply(drive).map(result -> drives.put(ID, result));
            }
            finally {
                drive.unlock();
            }
        });
    }

    /**
     * @return a Collection of previews of all created drives
     */
    @Override
    public Collection<DriveView> getDrives() {
        return Collections.unmodifiableCollection(drives.values());
    }

}

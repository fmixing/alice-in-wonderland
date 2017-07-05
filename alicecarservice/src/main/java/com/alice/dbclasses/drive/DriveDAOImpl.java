package com.alice.dbclasses.drive;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DriveDAOImpl implements DriveDAO {

    /**
     * Maps drives IDs to {@code Drive}
     */
    private final Map<Long, Drive> drives;

    public DriveDAOImpl() {
        drives = new ConcurrentHashMap<>();
    }

    /**
     * @param ID drive's ID
     * @return an {@code Optional} object which contains cloned {@code Drive} if a drive with this ID exists,
     * an empty Optional otherwise
     */
    @Override
    public Optional<Drive> getDriveByID(long ID) {
        return Optional.ofNullable(drives.get(ID)).map(Drive::cloneDrive);
    }

    /**
     * @param drive which is needed to be in DB
     * @return a Drive object which copy now contains in DB
     */
    @Override
    public Drive putDrive(Drive drive) {
        drives.put(drive.getDriveID(), drive.cloneDrive());
        return drive;
    }

    /**
     * @return a list of previews of all created drives
     */
    @Override
    public List<DriveView> getDrives() {
        return new ArrayList<>(drives.values());
    }

}

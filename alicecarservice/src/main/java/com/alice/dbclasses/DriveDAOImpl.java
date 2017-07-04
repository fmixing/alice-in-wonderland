package com.alice.dbclasses;

import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class DriveDAOImpl implements DriveDAO {

    /**
     * Maps drives IDs to {@code Drive}
     */
    private Map<Long, Drive> drives;

    DriveDAOImpl() {
        drives = new ConcurrentHashMap<>();
    }

    /**
     * @param ID drive's ID
     * @return a Drive object if the drive with this ID exists, null otherwise
     */
    @Override
    public Drive getDriveByID(long ID) {
        return drives.get(ID);
    }

    /**
     * @param drive which is needed to be in DB
     * @return a Drive object which now contains in DB
     */
    @Override
    public Drive putDrive(Drive drive) {
        drives.put(drive.getDriveID(), drive);
        return drive;
    }

    /**
     * @return a list of all created drives
     */
    @Override
    public List<Drive> getDrives() {
        return new ArrayList<>(drives.values());
    }

}

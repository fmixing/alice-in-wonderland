package com.test.db;

import com.test.cache.Cache;
import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import com.test.drive.User;
import com.test.drive.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UpdateDB {


    @Autowired
    private DriveRepository driveRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Contains drive which dates are in time interval [{@code Cache.fromDate}, {@code Cache.toDate}]
     */
    @Autowired
    private Cache cache;

    /**
     * Saves all users which are joined to drives in users table, saves all drives in drives table as a transaction
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDrives(List<com.alice.dbclasses.drive.Drive> drives) {
        List<User> dbUsers = drives.stream().map(com.alice.dbclasses.drive.Drive::getJoinedUsers).flatMap(Set::stream)
                .map(User::new).collect(Collectors.toList());

        userRepository.save(dbUsers);

        List<Drive> dbDrives = drives.stream().map(this::convertDrive).collect(Collectors.toList());

        driveRepository.save(dbDrives);

        cache.addDrives(dbDrives.stream().
                filter(this::isInInterval).
                collect(Collectors.toList()));
    }

    /**
     * Checks if drive can be put in cache
     */
    private boolean isInInterval(Drive drive) {
        return cache.getDateFrom() <= drive.getDate() &&  drive.getDate() <= cache.getDateTo();
    }

    /**
     * Converts database objects {@code com.alice.dbclasses.drive.Drive} to {@code Drive}
     */
    private Drive convertDrive(com.alice.dbclasses.drive.Drive drive) {
        Drive dbDrive = new  Drive(drive.getDriveID(), drive.getUserID(),
                drive.getFrom(), drive.getTo(), drive.getDate(), drive.getVacantPlaces());
        dbDrive.setJoinedUsers(drive.getJoinedUsers().stream().map(User::new).collect(Collectors.toSet()));
        return dbDrive;
    }

}

package com.alice.dbclasses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;


/**
 * TODO: exceptions?
 */

@Component
public class DriveService {

    @Autowired
    private DriveDAO driveDAO;

    @Autowired
    private UserDAO userDAO;

    /**
     * Creates a new unique drive ID
     */
    private AtomicLong id = new AtomicLong(0);

    /**
     * @return created object Drive which has been put to DB
     */
    public Drive addDrive(long userID, long from, long to, long date, int vacantPlaces) {
        long driveID = id.incrementAndGet();
        Drive drive = new Drive(driveID, userID, from, to, date, vacantPlaces);
        User user = userDAO.getUserByID(userID).cloneUser();
        user.addPostedDrive(driveID);
        userDAO.putUser(user);
        driveDAO.putDrive(drive);

        return drive;
    }

    /**
     * @param driveID a drive to which a user wants to join
     * @param userID user ID
     * @return a Drive to which the user joined
     */
    public Drive joinDrive(long driveID, long userID) {
        Drive drive = driveDAO.getDriveByID(driveID).cloneDrive();
        User user = userDAO.getUserByID(userID).cloneUser();
        user.addJoinedDrive(driveID);
        userDAO.putUser(user);
        driveDAO.putDrive(drive);
        return drive;
    }
}

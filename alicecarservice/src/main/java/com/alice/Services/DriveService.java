package com.alice.Services;

import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveDAO;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class DriveService {

    private final DriveDAO driveDAO;


    private final UserDAO userDAO;

    @Autowired
    public DriveService(UserDAO userDAO, DriveDAO driveDAO) {
        this.userDAO = userDAO;
        this.driveDAO = driveDAO;
    }

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
        User user = userDAO.getUserByID(userID).orElse(null);

        if (user == null)
            return null;

        user.addPostedDrive(driveID);
        userDAO.putUser(user);
        driveDAO.putDrive(drive);

        return drive;
    }

    /**
     * @param driveID a drive to which a user wants to join
     * @param userID user ID
     * @return a Drive to which the user joined, null if a user with this ID doesn't exist
     */
    public Drive joinDrive(long driveID, long userID) {
        Drive drive = driveDAO.getDriveByID(driveID).orElse(null);
        User user = userDAO.getUserByID(userID).orElse(null);

        if (drive == null || user == null)
            return null;

        user.addJoinedDrive(driveID);
        drive.addUser(userID);

        userDAO.putUser(user);
        return driveDAO.putDrive(drive);
    }

    /**
     * @param ID drive ID
     * @return a drive with this ID, null if a drive with this ID doesn't exist
     */
    public Drive getDrive(long ID) {
        Optional<Drive> drive = driveDAO.getDriveByID(ID);
        return drive.orElse(null);
    }

    /**
     * @return all the created drives
     */
    public List<DriveView> getAllDrives() {
        return driveDAO.getDrives();
    }
}

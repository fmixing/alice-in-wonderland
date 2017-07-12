package com.alice.services;

import com.alice.dbclasses.UpdateDB;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveDAO;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


import java.util.Collection;
import java.util.Optional;


@Component
public class DriveService {

    private final DriveDAO driveDAO;


    private final UserDAO userDAO;


    private final UpdateDB updateDB;

    @Autowired
    public DriveService(UserDAO userDAO, DriveDAO driveDAO, UpdateDB updateDB) {
        this.userDAO = userDAO;
        this.driveDAO = driveDAO;
        this.updateDB = updateDB;
    }

    /**
     * @return a {@code Result} object created created driveView with this data,
     * error message if a user with this userID doesn't exist
     */
    public Result<DriveView> addDrive(long userID, long from, long to, long date, int vacantPlaces) {

        Result<DriveView> res = new Result<>();

        Optional<UserView> userView = userDAO.modify(userID, user -> {

            driveDAO.createDrive(userID, from, to, date, vacantPlaces, drive -> {
                user.addPostedDrive(drive.getDriveID());
                updateDB.updateUserDrive(user, drive);
                driveDAO.putToCache(drive);
                res.setResult(drive);
            });

            return Optional.of(user);
        });

        if (!userView.isPresent()) {
            res.setMessage("User with ID " + userID + " doesn't exist");
        }

        return res;
    }

    /**
     * @param driveID a drive to which a user wants to join
     * @param userID user ID
     *
     * @return a {@code Result} object contains driveView to which the user joined,
     * error message if drive or user with this ID's doesn't exist
     */
    public Result<DriveView> joinDrive(long driveID, long userID) {
        Result<DriveView> result = new Result<>();
        Optional<DriveView> driveView = driveDAO.modify(driveID, drive -> {
            Optional<UserView> userView = userDAO.modify(userID, user -> {
                if (drive.getUserID() == userID) {
                    result.setMessage("User can't join to a drive which has created");
                    return Optional.empty();
                }
                if (drive.getJoinedUsers().contains(userID)) {
                    result.setMessage("You have already joined this drive");
                    return Optional.empty();
                }
                if (!drive.addUser(userID)) {
                    result.setMessage("Can't join to a drive with ID " + driveID + ", all seats are taken");
                    return Optional.empty();
                }
                user.addJoinedDrive(driveID);

                updateDB.updateUserDrive(user, drive);

                result.setResult(drive);

                driveDAO.putToCache(drive);

                return Optional.of(user);
            });
            if (!userView.isPresent()) {
                result.setMessage("User with ID " + userID + " doesn't exist");
                return Optional.empty();
            }
            return Optional.of(drive);
        });
        if (!driveView.isPresent())
            result.setMessage("Drive with ID " + driveID + " doesn't exist");
        return result;
    }

    /**
     * @param ID drive ID
     * @return a {@code Result} object contains driveView with this ID,
     * error message if a drive with this ID doesn't exist
     */
    public Result<DriveView> getDrive(long ID) {
        Result<DriveView> result = new Result<>();
        Optional<DriveView> drive = driveDAO.getDriveByID(ID);

        if (!drive.isPresent()) {
            result.setMessage("Drive with ID " + ID + " doesn't exist");
            return result;
        }
        result.setResult(drive.get());
        return result;
    }

    /**
     * @return all the created drives
     */
    public Collection<DriveView> getAllDrives() {
        return driveDAO.getDrives();
    }
}

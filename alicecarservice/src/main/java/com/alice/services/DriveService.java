package com.alice.services;

import com.alice.dbclasses.UpdateDB;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveDAO;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.CommonMetrics;
import com.alice.utils.Result;
import com.codahale.metrics.Timer;
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
     * @return a {@code Result} object contains created driveView with this data,
     * error message if a user with this userID doesn't exist
     */
    public Result<DriveView> addDrive(long userID, long from, long to, long date, int vacantPlaces) {

        final Timer.Context context = CommonMetrics.getTimerContext(DriveService.class, "addDrive-request");
        final Timer.Context contextAll = CommonMetrics.getTimerContext(UserService.class, "allOps-request");

        Result<DriveView> res;
        try {
            res = new Result<>();

            driveDAO.createDrive(userID, from, to, date, vacantPlaces, drive -> {
                Optional<UserView> userView = userDAO.modify(userID, user -> {

                    user.addPostedDrive(drive.getDriveID());
                    final Timer.Context contextDB = CommonMetrics.getTimerContext(DriveService.class, "updateUserDrive-request");
                    try {
                        updateDB.updateUserDrive(user, drive);
                    } finally {
                        contextDB.stop();
                    }
                    driveDAO.putToCache(drive);
                    res.setResult(drive);

                    return Optional.of(user);
                });

                if (!userView.isPresent()) {
                    res.setMessage("User with ID " + userID + " doesn't exist");

                }
            });
        } finally {
            context.stop();
            contextAll.stop();
        }

        return res;
    }

    /**
     * @param driveID a drive to which a user wants to join
     * @param userID  user ID
     * @return a {@code Result} object contains driveView to which the user joined,
     * error message if drive or user with this ID's doesn't exist
     */
    public Result<DriveView> joinDrive(long driveID, long userID) {

        final Timer.Context context = CommonMetrics.getTimerContext(DriveService.class, "joinDrive-request");
        final Timer.Context contextAll = CommonMetrics.getTimerContext(UserService.class, "allOps-request");

        try {
            Result<DriveView> result = new Result<>();
            Optional<DriveView> driveView = driveDAO.modify(driveID, drive -> {
                Optional<UserView> userView = userDAO.modify(userID, user -> {
                    if (drive.getUserID() == userID) {
                        result.setMessage("User can't join to a drive which they created");
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

                    final Timer.Context contextDB = CommonMetrics.getTimerContext(DriveService.class, "updateUserDrive-request");
                    try {
                        updateDB.updateUserDrive(user, drive);
                    } finally {
                        contextDB.stop();
                    }

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
        } finally {
            context.stop();
            contextAll.stop();
        }
    }

    /**
     * @param ID drive ID
     * @return a {@code Result} object contains driveView with this ID,
     * error message if a drive with this ID doesn't exist
     */
    public Result<DriveView> getDrive(long ID) {
        final Timer.Context context = CommonMetrics.getTimerContext(DriveService.class, "getDrive-request");
        final Timer.Context contextAll = CommonMetrics.getTimerContext(UserService.class, "allOps-request");

        try {
            Result<DriveView> result = new Result<>();
            Optional<DriveView> drive = driveDAO.getDriveByID(ID);

            if (!drive.isPresent()) {
                result.setMessage("Drive with ID " + ID + " doesn't exist");
                return result;
            }
            result.setResult(drive.get());
            return result;
        } finally {
            context.stop();
            contextAll.stop();
        }
    }

    /**
     * @return all the created drives
     */
    public Collection<DriveView> getAllDrives() {

        final Timer.Context context = CommonMetrics.getTimerContext(DriveService.class, "getAllDrives-request");

        try {
            return driveDAO.getDrives();
        } finally {
            context.stop();
        }
    }
}

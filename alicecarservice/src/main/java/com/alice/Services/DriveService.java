package com.alice.Services;

import com.alice.dao.Result;
import com.alice.dao.ResultImpl;
import com.alice.dbclasses.drive.DriveDAOImpl;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.UserDAOImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class DriveService {

    private final DriveDAOImpl driveDAO;

    private final UserDAOImpl userDAO;

    @Autowired
    public DriveService(UserDAOImpl userDAO, DriveDAOImpl driveDAO) {
        this.userDAO = userDAO;
        this.driveDAO = driveDAO;
    }

    /**
     * Creates a new unique drive ID
     */
    // скорее всего этот кусочек лучше перенести на уровень ниже -- в DAO
    private AtomicLong id = new AtomicLong(0);

    /**
     * @return created object Drive which has been put to DB
     */
    public Result<DriveView> addDrive(long userID, long from, long to, long date, int vacantPlaces) {
        return userDAO.modifyWithResult(userID, (result, user) -> {
            DriveView driveView = driveDAO.addDrive(userID, from, to, date, vacantPlaces);
            user.addPostedDrive(driveView.getDriveID());
            result.setResult(driveView);
            return Optional.of(user);
        });
    }

    /**
     * @param driveID a drive to which a user wants to join
     * @param userID user ID
     * @return a Drive to which the user joined, null if a user with this ID doesn't exist
     */
    public Result<DriveView> joinDrive(long driveID, long userID) {
        // координатор
        return userDAO.modifyWithResult(userID, (result, user) ->
        {
            // плагин
            driveDAO.modify(userID, drive ->
            {
                if (drive.addUser(userID))
                {
                    user.addJoinedDrive(driveID);
                    result.setResult(drive);
                    return Optional.of(drive);
                }
                else
                {
                    result.setError("No enough vacant places");
                    return Optional.empty();
                }
            });
            return Optional.of(user);
        });
    }

    /**
     * @param ID drive ID
     * @return a drive with this ID, null if a drive with this ID doesn't exist
     */
    public Result<DriveView> getDrive(long ID) {
        Optional<DriveView> drive = driveDAO.getViewByID(ID);
        Result<DriveView> driveViewResult = new ResultImpl<>();
        drive.ifPresent(driveViewResult::setResult);
        return driveViewResult;
    }

    /**
     * @return all the created drives
     */
    public Collection<DriveView> getAllDrives() {
        return driveDAO.getAllViews();
    }
}

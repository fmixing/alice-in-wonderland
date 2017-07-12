package com.alice.dbclasses;

import com.alice.dbclasses.user.UserView;
import com.alice.services.DriveService;
import com.alice.services.LogPassService;
import com.alice.services.UserService;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveDAO;
import com.alice.dbclasses.drive.DriveDAOImpl;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserDAOImpl;
import com.alice.utils.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class DriveServiceTest {

    private DriveDAO driveDAO = new DriveDAOImpl(null, null);
    private UserDAO userDAO = new UserDAOImpl(null, null);
    private UpdateDB updateDB = new UpdateDB();
    private DriveService driveService = new DriveService(userDAO, driveDAO, updateDB);
    private LogPassService logPassService = new LogPassService();
    private UserService userService = new UserService(userDAO, logPassService, updateDB);

    private String login = "abc";
    private String password = "123";
    private long ID;
    private long date;

    @Before
    public void createUser() {
        ID = userService.addUser(login, password).getResult().getUserID();
        date = new Date().getTime();
    }


    @Test
    public void testAddingDrive() {
        Result<DriveView> drive = driveService.addDrive(ID, 1, 2, date,3);
        UserView user = userService.getUser(ID).getResult();
        assertNotEquals("This drive should have been created", null, drive.getResult());
        assertNotEquals("This user should have been created",null, user);
        assertEquals("Wrong driveID", 1L, drive.getResult().getDriveID());
        assertEquals("Wrong amount of posted drives", 1, user.getPostedDrives().size());
    }

    @Test
    public void testAddingToWrongUser() {
        Result<DriveView> drive = driveService.addDrive(2, 1, 2, date,3);
        assertEquals("This drive should not have been created", null, drive.getResult());
        assertEquals("User with ID 2 doesn't exist", drive.getMessage());

    }

    @Test
    public void testAddingUserToDrive() {
        userService.addUser("aaa", "bbb");
        driveService.addDrive(1, 1, 2, date,3);
        DriveView drive = driveService.joinDrive(1, 2).getResult();
        UserView user = userService.getUser(2).getResult();
        assertNotEquals("This drive should have been created",null, drive);
        assertEquals("User should have been added to this drive",1, drive.getJoinedUsers().size());
        assertEquals("Drive should have been added to this user", 1, user.getJoinedDrives().size());

    }

    @Test
    public void testAddingUserToWrongDrive() {
        userService.addUser("aaa", "bbb");
        driveService.addDrive(1, 1, 2, date,3);
        Result<DriveView> drive = driveService.joinDrive(2, 2);
        assertEquals("This drive should not have been created",null, drive.getResult());
        assertEquals("Drive with ID 2 doesn't exist", drive.getMessage());
    }

    @Test
    public void testAddingWrongUserToDrive() {
        driveService.addDrive(1, 1, 2, date,3);
        Result<DriveView> drive = driveService.joinDrive(1, 3);
        assertEquals("This user shouldn't have been added to this drive",null, drive.getResult());
        assertEquals("User with ID 3 doesn't exist", drive.getMessage());
    }

    @Test
    public void testAddingManyUsersToDrive() {
        for (int i = 0; i < 4; i++) {
            userService.addUser("abc" + i, "123" + i);
        }

        driveService.addDrive(1, 1, 2, date,3);
        for (int i = 0; i < 3; i++) {
            driveService.joinDrive(1, i + 1);
        }

        Result<DriveView> drive = driveService.joinDrive(1, 4);
        assertEquals("This user should not have been added to drive",null, drive.getResult());
        assertEquals("Can't join to drive with ID 1, all seats are taken", drive.getMessage());
        assertEquals(0, userService.getUser(4).getResult().getJoinedDrives().size());
    }

    @Test
    public void testGettingDrive() {
        DriveView drive = driveService.addDrive(1, 1, 2, date,3).getResult();
        DriveView gotDrive = driveService.getDrive(1).getResult();
        assertEquals("Wrong driveID", drive.getDriveID(), gotDrive.getDriveID());
        assertEquals("Wrong userID", drive.getUserID(), gotDrive.getUserID());
        assertEquals("Wrong from", drive.getFrom(), gotDrive.getFrom());
        assertEquals("Wrong to", drive.getTo(), gotDrive.getTo());
        assertEquals("Wrong date", date, gotDrive.getDate());
        assertEquals("Wrong amount of vacant places", drive.getVacantPlaces(), gotDrive.getVacantPlaces());
    }

    @Test
    public void testGettingWrongDrive() {
        DriveView drive = driveService.addDrive(1, 1, 2, date,3).getResult();
        Result<DriveView> gotDrive = driveService.getDrive(2);
        assertEquals("This drive should not have been created",null, gotDrive.getResult());
        assertEquals("Drive with ID 2 doesn't exist", gotDrive.getMessage());

    }

    @Test
    public void testGettingAllDrives() {
        for (int i = 0; i < 10000; i++) {
            userService.addUser("abc" + i, "123" + i);
        }

        for (int i = 0; i < 10000; i++) {
            driveService.addDrive(i + 1, 1, 2, date, 3);
        }

        Collection<DriveView> driveViews = driveService.getAllDrives();

        List<DriveView> drives = new ArrayList<>(driveService.getAllDrives());

        for (int i = 0; i < 10000; i++) {
            assertNotEquals("Drive with this ID should exist", null, driveService.getDrive(i + 1));
            assertEquals("Drive with this ID should exist", i + 1, drives.get(i).getDriveID());
        }
    }
}

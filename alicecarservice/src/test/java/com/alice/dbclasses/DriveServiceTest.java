package com.alice.dbclasses;

import com.alice.Services.DriveService;
import com.alice.Services.LogPassService;
import com.alice.Services.UserService;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveDAO;
import com.alice.dbclasses.drive.DriveDAOImpl;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserDAOImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DriveServiceTest {

    private DriveDAO driveDAO = new DriveDAOImpl();
    private UserDAO userDAO = new UserDAOImpl();
    private DriveService driveService = new DriveService(userDAO, driveDAO);
    private LogPassService logPassService = new LogPassService();
    private UserService userService = new UserService(userDAO, logPassService);

    private String login = "abc";
    private String password = "123";
    private long ID;
    private long date;

    @Before
    public void createUser() {
        ID = userService.addUser(login, password).getUserID();
        date = new Date().getTime();
    }


    @Test
    public void testAddingDrive() {
        Drive drive = driveService.addDrive(ID, 1, 2, date,3);
        User user = userService.getUser(ID);
        assertNotEquals("This drive should have been created", null, drive);
        assertNotEquals("This user should have been created",null, user);
        assertEquals("Wrong driveID", 1L, drive.getDriveID());
        assertEquals("Wrong amount of posted drives", 1, user.getPostedDrives().size());
        assertNotEquals("Each hashcode should be unique", drive.hashCode(), driveService.getDrive(drive.getDriveID()));
    }

    @Test
    public void testAddingToWrongUser() {
        Drive drive = driveService.addDrive(2, 1, 2, date,3);
        assertEquals("This drive should not have been created", null, drive);
    }

    @Test
    public void testAddingUserToDrive() {
        userService.addUser("aaa", "bbb");
        driveService.addDrive(1, 1, 2, date,3);
        Drive drive = driveService.joinDrive(1, 2);
        User user = userService.getUser(2);
        assertNotEquals("This drive should have been created",null, drive);
        assertEquals("User should have been added to this drive",1, drive.getJoinedUsers().size());
        assertEquals("Drive should have been added to this user", 1, user.getJoinedDrives().size());

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


        Drive drive = driveService.joinDrive(1, 4);
        assertEquals("This user should not have been added to drive",null, drive);
        assertEquals(0, userService.getUser(4).getJoinedDrives().size());
    }

    @Test
    public void testGettingDrive() {
        Drive drive = driveService.addDrive(1, 1, 2, date,3);
        Drive gotDrive = driveService.getDrive(1);
        assertEquals("Wrong driveID", drive.getDriveID(), gotDrive.getDriveID());
        assertEquals("Wrong userID", drive.getUserID(), gotDrive.getUserID());
        assertEquals("Wrong from", drive.getFrom(), gotDrive.getFrom());
        assertEquals("Wrong to", drive.getTo(), gotDrive.getTo());
        assertEquals("Wrong date", date, gotDrive.getDate());
        assertEquals("Wrong amount of vacant places", drive.getVacantPlaces(), gotDrive.getVacantPlaces());
    }

    @Test
    public void testGettingAllDrives() {
        for (int i = 0; i < 10000; i++) {
            userService.addUser("abc" + i, "123" + i);
        }

        for (int i = 0; i < 10000; i++) {
            driveService.addDrive(i + 1, 1, 2, date, 3);
        }

        List<DriveView> drives = driveService.getAllDrives();

        for (int i = 0; i < 10000; i++) {
            assertNotEquals("User with this ID should exist", null, driveService.getDrive(i + 1));
            assertEquals("User with this ID should exist", i + 1, drives.get(i).getDriveID());
        }
    }
}

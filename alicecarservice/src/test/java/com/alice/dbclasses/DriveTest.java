package com.alice.dbclasses;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class DriveTest {

    private Drive drive;
    private long date;

    @Before
    public void createDrive() {
        date = new Date().getTime();
        drive = new Drive(1, 1, 1, 2, date, 3);
    }

    @Test
    public void test01_testDriveInfo() {
        System.err.println("=== Running test01_testDriveInfo");
        assertEquals("Wrong driveID: expected 1, found " + drive.getDriveID(), 1, drive.getDriveID());
        assertEquals("Wrong userID: expected 1, found " + drive.getUserID(), 1, drive.getUserID());
        assertEquals("Wrong from: expected 1, found " + drive.getFrom(), 1, drive.getFrom());
        assertEquals("Wrong to: expected 2, found " + drive.getTo(), 2, drive.getTo());
        assertEquals("Wrong date: expected "+ date +", found " + drive.getDate(), date, drive.getDate());
        assertEquals("Wrong amount of vacant places: expected 3, found " + drive.getVacantPlaces(), 3, drive.getVacantPlaces());
    }

    @Test
    public void test02_testAddingUser() {
        System.err.println("=== Running test02_testAddingUser");
        boolean successful = drive.addUser(1);
        assertTrue("Can't add user drive", successful);
        assertEquals("Wrong amount of users: expected 1, found " + drive.getUsersNumber(), 1, drive.getUsersNumber());
    }

    @Test
    public void test03_testAddingSameUserTwice() {
        System.err.println("=== Running test03_testAddingSameUserTwice");
        drive.addUser(1);
        boolean successful = drive.addUser(1);
        assertFalse("User's ID wasn't added to this drive for the first time", successful);
        assertEquals("Wrong amount of added users: expected 1, found " + drive.getUsersNumber(), 1, drive.getUsersNumber());
    }

    @Test
    public void test04_testManyUsers() {
        System.err.println("=== Running test04_testManyUsers");
        boolean successful;
        for (int i = 0; i < 3; i++) {
            successful = drive.addUser(i);
            assertTrue("Can't add user with ID " + i, successful);
        }
        successful = drive.addUser(3);
        assertFalse("One of the users wasn't added", successful);
        assertEquals("Wrong amount of posted drives: expected 3, found " + drive.getUsersNumber(), 3, drive.getUsersNumber());
    }

    @Test
    public void test05_testCloningDrive() {
        System.err.println("=== Running test05_testCloningDrive");
        for (int i = 0; i < 3; i++) {
            drive.addUser(i);
        }
        Drive clonedDrive = drive.cloneDrive();
        assertEquals("Wrong driveID: expected 1, found " + clonedDrive.getDriveID(), 1, clonedDrive.getDriveID());
        assertEquals("Wrong userID: expected 1, found " + clonedDrive.getUserID(), 1, clonedDrive.getUserID());
        assertEquals("Wrong from: expected 1, found " + clonedDrive.getFrom(), 1, clonedDrive.getFrom());
        assertEquals("Wrong to: expected 2, found " + clonedDrive.getTo(), 2, clonedDrive.getTo());
        assertEquals("Wrong date: expected " + date +", found " + clonedDrive.getDate(), date, clonedDrive.getDate());
        assertEquals("Wrong amount of vacant places: expected 3, found " + clonedDrive.getVacantPlaces(), 3, clonedDrive.getVacantPlaces());
        assertEquals("Wrong amount of posted drives: expected 3, found " + clonedDrive.getUsersNumber(), 3, clonedDrive.getUsersNumber());
    }

    @Test
    public void test06_testJoinedSet() {
        System.err.println("=== Running test06_testJoinedSet");
        for (int i = 0; i < 3; i++) {
            drive.addUser(i);
        }
        List<Long> joinedSet = new ArrayList<>(drive.getJoinedUsers());
        for (int i = 0; i < 3; i++) {
            assertEquals("Wrong joined userID: expected " + i + ", found " + joinedSet.get(i), (long) i, (long) joinedSet.get(i));
        }
    }
}

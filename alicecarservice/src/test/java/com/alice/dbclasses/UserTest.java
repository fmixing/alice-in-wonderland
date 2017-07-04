package com.alice.dbclasses;

import org.junit.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class UserTest {

    private User user;

    @Before
    public void createUser() {
        user = new User(1);
    }

    @Test
    public void test01_testUserID() {
        System.err.println("=== Running test01_testUserID");
        assertEquals("Wrong userID: expected 1, found " + user.getUserID(), 1, user.getUserID());
    }

    @Test
    public void test02_testAddingPostedDrive() {
        System.err.println("=== Running test02_testAddingPostedDrive");
        boolean successful = user.addPostedDrive(1);
        assertTrue("Can't add posted drive", successful);
        assertEquals("Wrong amount of posted drives: expected 1, found " + user.getPostedDrives().size(), 1, user.getPostedDrives().size());
    }

    @Test
    public void test03_testAddingSamePostedDriveTwice() {
        System.err.println("=== Running test03_testAddingSamePostedDriveTwice");
        user.addPostedDrive(1);
        boolean successful = user.addPostedDrive(1);
        assertFalse("Posted drive's ID wasn't added to this user for the first time", successful);
        assertEquals("Wrong amount of posted drives: expected 1, found " + user.getPostedDrives().size(), 1, user.getPostedDrives().size());
    }

    @Test
    public void test04_testAddingManyPostedDrives() {
        System.err.println("=== Running test04_testAddingManyPostedDrives");
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        assertEquals("Wrong amount of posted drives: expected 10000, found " + user.getPostedDrives().size(), 10000, user.getPostedDrives().size());
    }

    @Test
    public void test05_testAddingJoinedDrive() {
        System.err.println("=== Running test05_testAddingJoinedDrive");
        boolean successful = user.addJoinedDrive(1);
        assertTrue("Can't add posted drive", successful);
        assertEquals("Wrong amount of joined drives: expected 1, found " + user.getJoinedDrives().size(), 1, user.getJoinedDrives().size());
    }

    @Test
    public void test06_testAddingSameJoinedDriveTwice() {
        System.err.println("=== Running test06_testAddingSameJoinedDriveTwice");
        user.addJoinedDrive(1);
        boolean successful = user.addJoinedDrive(1);
        assertFalse("Posted drive's ID wasn't added to this user for the first time", successful);
        assertEquals("Wrong amount of joined drives: expected 1, found " + user.getJoinedDrives().size(), 1, user.getJoinedDrives().size());
    }

    @Test
    public void test07_testAddingManyJoinedDrives() {
        System.err.println("=== Running test07_testAddingManyJoinedDrives");
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        assertEquals("Wrong amount of joined drives: expected 10000, found " + user.getJoinedDrives().size(), 10000, user.getJoinedDrives().size());
    }

    @Test
    public void test08_testCloningUser() {
        System.err.println("=== Running test08_testCloningUser");
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        User clonedUser = user.cloneUser();
        assertEquals("Wrong userID: expected 1, found " + clonedUser.getUserID(), 1, clonedUser.getUserID());
        assertEquals("Wrong amount of joined drives: expected 10000, found " + clonedUser.getJoinedDrives().size(),10000, clonedUser.getJoinedDrives().size());
        assertEquals("Wrong amount of posted drives: expected 10000, found " + clonedUser.getPostedDrives().size(),10000, clonedUser.getPostedDrives().size());
    }

    @Test
    public void test09_testJoinedSet() {
        System.err.println("=== Running test09_testJoinedSet");
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        List<Long> joinedSet = new ArrayList<>(user.getJoinedDrives());
        for (int i = 0; i < 10000; i++) {
            assertEquals("Wrong value of joined set: expected " + i + ", found " + joinedSet.get(i), (long) i, (long) joinedSet.get(i));
        }
    }

    @Test
    public void test10_testPostedSet() {
        System.err.println("=== Running test10_testPostedSet");
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        List<Long> postedSet = new ArrayList<>(user.getPostedDrives());
        for (int i = 0; i < 10000; i++) {
            assertEquals("Wrong value of joined set: expected " + i + ", found " + postedSet.get(i), (long) i, (long) postedSet.get(i));
        }
    }

}
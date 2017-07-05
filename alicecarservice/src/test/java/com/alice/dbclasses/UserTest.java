package com.alice.dbclasses;

import com.alice.dbclasses.user.User;
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
    public void testAddingPostedDrive() {
        boolean successful = user.addPostedDrive(1);
        assertTrue("Can't add posted drive", successful);
        assertEquals("Wrong amount of posted drives", 1, user.getPostedDrives().size());
    }

    @Test
    public void testAddingSamePostedDriveTwice() {
        user.addPostedDrive(1);
        boolean successful = user.addPostedDrive(1);
        assertFalse("Posted drive's ID wasn't added to this user for the first time", successful);
        assertEquals("Wrong amount of posted drives", 1, user.getPostedDrives().size());
    }

    @Test
    public void testAddingManyPostedDrives() {
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        assertEquals("Wrong amount of posted drives", 10000, user.getPostedDrives().size());
    }

    @Test
    public void testAddingJoinedDrive() {
        boolean successful = user.addJoinedDrive(1);
        assertTrue("Can't add posted drive", successful);
        assertEquals("Wrong amount of joined drives", 1, user.getJoinedDrives().size());
    }

    @Test
    public void testAddingSameJoinedDriveTwice() {
        user.addJoinedDrive(1);
        boolean successful = user.addJoinedDrive(1);
        assertFalse("Posted drive's ID wasn't added to this user for the first time", successful);
        assertEquals("Wrong amount of joined drives", 1, user.getJoinedDrives().size());
    }

    @Test
    public void testAddingManyJoinedDrives() {
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        assertEquals("Wrong amount of joined drives", 10000, user.getJoinedDrives().size());
    }

    @Test
    public void testCloningUser() {
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        User clonedUser = user.cloneUser();
        assertEquals("Wrong userID", 1, clonedUser.getUserID());
        assertEquals("Wrong amount of joined drives",10000, clonedUser.getJoinedDrives().size());
        assertEquals("Wrong amount of posted drives",10000, clonedUser.getPostedDrives().size());
    }

    @Test
    public void testJoinedSet() {
        for (int i = 0; i < 10000; i++) {
            user.addJoinedDrive(i);
        }
        List<Long> joinedSet = new ArrayList<>(user.getJoinedDrives());
        for (int i = 0; i < 10000; i++) {
            assertEquals("Wrong value of joined set", (long) i, (long) joinedSet.get(i));
        }
    }

    @Test
    public void testPostedSet() {
        for (int i = 0; i < 10000; i++) {
            user.addPostedDrive(i);
        }
        List<Long> postedSet = new ArrayList<>(user.getPostedDrives());
        for (int i = 0; i < 10000; i++) {
            assertEquals("Wrong value of joined set", (long) i, (long) postedSet.get(i));
        }
    }

}
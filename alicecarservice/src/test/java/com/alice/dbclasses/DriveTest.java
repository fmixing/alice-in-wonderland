package com.alice.dbclasses;

import com.alice.dbclasses.drive.Drive;
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
    public void testAddingUser() {
        boolean successful = drive.addUser(1);
        assertTrue("Can't add user drive", successful);
        assertEquals("Wrong amount of users", 1, drive.getUsersNumber());
    }

    @Test
    public void testAddingSameUserTwice() {
        drive.addUser(1);
        boolean successful = drive.addUser(1);
        assertFalse("User's ID wasn't added to this drive for the first time", successful);
        assertEquals("Wrong amount of added users", 1, drive.getUsersNumber());
    }

    @Test
    public void testManyUsers() {
        boolean successful;
        for (int i = 0; i < 3; i++) {
            successful = drive.addUser(i);
            assertTrue("Can't add user with ID " + i, successful);
        }
        successful = drive.addUser(3);
        assertFalse("One of the users wasn't added", successful);
        assertEquals("Wrong amount of posted drives", 3, drive.getUsersNumber());
    }

    @Test
    public void testJoinedSet() {
        for (int i = 0; i < 3; i++) {
            drive.addUser(i);
        }
        List<Long> joinedSet = new ArrayList<>(drive.getJoinedUsers());
        for (int i = 0; i < 3; i++) {
            assertEquals("Wrong joined userID", (long) i, (long) joinedSet.get(i));
        }
    }
}

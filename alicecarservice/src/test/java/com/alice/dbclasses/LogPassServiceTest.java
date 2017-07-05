package com.alice.dbclasses;

import com.alice.Services.LogPassService;
import org.junit.Test;

import java.util.Optional;

import static org.junit.Assert.*;

public class LogPassServiceTest {

    private LogPassService logPassService = new LogPassService();

    private String login = "abc";
    private String password = "123";

    @Test
    public void testCreatingUserData() {
        Optional<Long> id = logPassService.createNewUser(login, password);
        assertTrue("User with this ID should have been created", id.isPresent());
        assertEquals("Wrong userID", 1L, (long) id.get());
    }

    @Test
    public void testCreatingSameUserDataTwice() {
        Optional<Long> id = logPassService.createNewUser(login, password);
        Optional<Long> id1 = logPassService.createNewUser(login, password);
        assertFalse("User with this ID should not have been created", id1.isPresent());
    }

    @Test
    public void testCreatingManyUsers() {
        for (int i = 0; i < 10000; i++) {
            logPassService.createNewUser("abc" + i, "123" + i);
        }
        for (int i = 0; i < 10000; i++) {
            Optional<Long> userID = logPassService.getUserID("abc" + i, "123" + i);
            assertTrue("User with this ID should have been created", userID.isPresent());
            assertEquals("Wrong userID", (long) (i + 1), (long) userID.get());
        }
    }
}

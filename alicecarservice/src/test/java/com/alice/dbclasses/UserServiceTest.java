package com.alice.dbclasses;

import com.alice.Services.LogPassService;
import com.alice.Services.UserService;
import com.alice.dbclasses.user.User;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserDAOImpl;
import com.alice.dbclasses.user.UserView;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UserServiceTest {

    private UserDAO userDAO = new UserDAOImpl();
    private LogPassService logPassService = new LogPassService();
    private UserService userService = new UserService(userDAO, logPassService);

    private String login = "abc";
    private String password = "123";


    @Test
    public void testAddingUser() {
        User user = userService.addUser(login, password);
        assertEquals("Wrong userID: expected 1, found " + user.getUserID(), 1, user.getUserID());
    }

    @Test
    public void testAddingSameUser() {
        User user = userService.addUser(login, password);
        User add_user = userService.addUser(login, password);
        assertEquals("User with login abc should already exist", null, add_user);
    }

    @Test
    public void testGettingUser() {
        User user = userService.addUser(login, password);
        User get_user_id1 = userService.getUser(1);
        User get_user_id2 = userService.getUser(2);
        assertNotEquals("User with this ID should already exist", null, get_user_id1);
        assertEquals("User with this ID should not exist", null, get_user_id2);
        assertNotEquals("Got user should not have the same hashcode", user.hashCode(), get_user_id1.hashCode());
        // делает то же самое, что и строка выше
        assertNotSame(user, get_user_id1);
    }

    @Test
    public void testGettingAllUsers() {
        for (int i = 0; i < 10000; i++) {
            userService.addUser("abc" + i, "123" + i);
        }

        List<UserView> users = userService.getAllUsers();

        for (int i = 0; i < 10000; i++) {
            assertNotEquals("User with this ID should exist", null, userService.getUser(i + 1));
            assertEquals("User with this ID should exist", i + 1, users.get(i).getUserID());
        }
    }

}

package com.alice.dbclasses;

import com.alice.services.LogPassService;
import com.alice.services.UserService;
import com.alice.dbclasses.user.UserDAO;
import com.alice.dbclasses.user.UserDAOImpl;
import com.alice.dbclasses.user.UserView;
import com.alice.utils.Result;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.*;

public class UserServiceTest {

    private UserDAO userDAO = new UserDAOImpl(null, null);
    private LogPassService logPassService = new LogPassService();
    private UpdateDB updateDB = new UpdateDB();
    private UserService userService = new UserService(userDAO, logPassService, updateDB);

    private String login = "abc";
    private String password = "123";


    @Test
    public void testAddingUser() {
        UserView user = userService.addUser(login, password).getResult();
        assertEquals("Wrong userID: expected 1, found " + user.getUserID(), 1, user.getUserID());
    }

    @Test
    public void testAddingSameUser() {
        UserView user = userService.addUser(login, password).getResult();
        Result<UserView> add_user = userService.addUser(login, password);
        assertEquals("User with login abc should already exist", null, add_user.getResult());
        assertEquals("User with login abc already exists", add_user.getMessage());
    }

    @Test
    public void testGettingUser() {
        UserView user = userService.addUser(login, password).getResult();
        UserView get_user_id1 = userService.getUser(1).getResult();
        Result<UserView> get_user_id2 = userService.getUser(2);
        assertNotEquals("User with this ID should already exist", null, get_user_id1);
        assertEquals("User with this ID should not exist", null, get_user_id2.getResult());
        assertEquals("User with ID 2 doesn't exist", get_user_id2.getMessage());
    }

    @Test
    public void testGettingAllUsers() {
        for (int i = 0; i < 10000; i++) {
            userService.addUser("abc" + i, "123" + i);
        }

        Collection<UserView> users = userService.getAllUsers();

        List<UserView> userViews = new ArrayList<>(users);

        for (int i = 0; i < 10000; i++) {
            assertNotEquals("User with this ID should exist", null, userService.getUser(i + 1));
            assertEquals("User with this ID should exist", i + 1, userViews.get(i).getUserID());
        }
    }

}

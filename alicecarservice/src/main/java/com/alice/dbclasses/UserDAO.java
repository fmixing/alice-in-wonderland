package com.alice.dbclasses;

import java.util.List;

public interface UserDAO {

    User getUserByID(long ID);

    User putUser(User user);

    List<User> getUsers();
}

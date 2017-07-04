package com.alice.dbclasses;

import java.util.List;

public interface UserDAO {

    public User getUserByID(long ID);

    public User putUser(User user);

    public List<User> getUsers();
}

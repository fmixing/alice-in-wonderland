package com.alice.dbclasses;

public interface UserDAO {

    public User getUserByID(int ID);

    public boolean putUser(int ID, User user);
}

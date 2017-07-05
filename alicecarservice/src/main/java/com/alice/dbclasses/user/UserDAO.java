package com.alice.dbclasses.user;

import java.util.List;
import java.util.Optional;

public interface UserDAO {

    Optional<User> getUserByID(long ID);

    User putUser(User user);

    List<UserView> getUsers();
}

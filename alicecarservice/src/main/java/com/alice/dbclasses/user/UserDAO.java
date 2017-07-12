package com.alice.dbclasses.user;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface UserDAO {

    Optional<UserView> getUserByID(long ID);

    Optional<UserView> modify(long ID, Function<User, Optional<User>> mapper);

    UserView createUser(long ID, Consumer<User> mapper);

    Collection<UserView> getUsers();

    void putToCache(User user);
}

package com.alice.dbclasses.user;

import java.util.Set;

public interface UserView {

    long getUserID();

    Set<Long> getJoinedDrives();

    Set<Long> getPostedDrives();
}

package com.alice.dbclasses.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable, UserView {

    /**
     * Users ID
     */
    private final long userID;

    /**
     * Drives which have been posted by this user
     */
    private final Set<Long> postedDrives;

    /**
     * Drives to which this user joined
     */
    private final Set<Long> joinedDrives;


    public User(long userID) {
        this.userID = userID;
        postedDrives = Collections.synchronizedSet(new HashSet<>());
        joinedDrives = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * @return a copy of the current user
     */
    public User cloneUser() {
        User user = new User(this.userID);
        for (Long ID : postedDrives) {
            user.addPostedDrive(ID);
        }
        for (Long ID : joinedDrives) {
            user.addJoinedDrive(ID);
        }
        return user;
    }

    /**
     * @return true if adding to {@code postedDrives} was successful
     */
    public boolean addPostedDrive(long driveID) {
        return postedDrives.add(driveID);
    }

    /**
     * @return true if adding to {@code joinedDrives} was successful
     */
    public boolean addJoinedDrive(long driveID) {
        return joinedDrives.add(driveID);
    }

    @Override
    public long getUserID() {
        return userID;
    }

    @Override
    public Set<Long> getJoinedDrives() {
        return Collections.unmodifiableSet(joinedDrives);
    }

    @Override
    public Set<Long> getPostedDrives() {
        return Collections.unmodifiableSet(postedDrives);
    }
}
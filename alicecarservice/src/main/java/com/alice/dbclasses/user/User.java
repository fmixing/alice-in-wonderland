package com.alice.dbclasses.user;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class User extends ReentrantLock implements Serializable, UserView {

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
        postedDrives = new HashSet<>();
        joinedDrives = new HashSet<>();
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

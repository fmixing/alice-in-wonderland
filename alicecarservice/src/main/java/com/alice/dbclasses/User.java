package com.alice.dbclasses;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class User implements Serializable {

    /**
     * Users ID
     */
    private long userID;

    /**
     * Drives which have been posted by this user
     */
    private Set<Long> postedDrives;

    /**
     * Drives to which this user joined
     */
    private Set<Long> joinedDrives;


    User(long userID) {
        this.userID = userID;
        postedDrives = new HashSet<>();
        joinedDrives = new HashSet<>();
    }

    /**
     * @return true if adding to {@code postedDrives} was successful
     */
    public boolean addPostedDrive(long driveID) {
        synchronized (postedDrives) {
            if (!postedDrives.add(driveID))
                return false;
        }
        return true;
    }

    /**
     * @return true if adding to {@code joinedDrives} was successful
     */
    public boolean addJoinedDrive(long driveID) {
        synchronized (joinedDrives) {
            if (!joinedDrives.add(driveID))
                return false;
        }
        return true;
    }

    public long getUserID() {
        return userID;
    }

    public Set<Long> getJoinedDrives() {
        return joinedDrives;
    }

    public Set<Long> getPostedDrives() {
        return postedDrives;
    }
}

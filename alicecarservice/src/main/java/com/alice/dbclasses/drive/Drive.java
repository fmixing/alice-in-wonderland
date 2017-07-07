package com.alice.dbclasses.drive;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;


public class Drive extends ReentrantLock implements Serializable, DriveView {

    /**
     * Drive's ID
     */
    private final long driveID;

    /**
     * Who posted a drive
     */
    private final long userID;

    /**
     * Starting point
     */
    private final long from;

    /**
     * Destination point
     */
    private final long to;

    /**
     * Date of drive
     */
    private final long date;

    /**
     * Count of vacant place
     */

    private final int vacantPlaces;

    /**
     * Set of joined users IDs
     */
    private final Set<Long> joinedUsers;

    public Drive(long driveID, long userID, long from, long to, long date, int vacantPlaces) {
        this.driveID = driveID;
        this.userID = userID;
        this.from = from;
        this.to = to;
        this.date = date;
        this.vacantPlaces = vacantPlaces;
        joinedUsers = new HashSet<>();

    }

    /**
     * Adds a user to a drive
     *
     * @return true if the attempt was successful
     */
    public boolean addUser(long joinedUserID) {
        return (joinedUsers.size() < vacantPlaces) && joinedUsers.add(joinedUserID);
    }

    /**
     * @return a copy of the current drive
     */
    public Drive cloneDrive() {
        Drive drive = new Drive(this.driveID, this.userID, this.from, this.to, this.date, this.vacantPlaces);
        for (Long userID : joinedUsers) {
            drive.addUser(userID);
        }
        return drive;
    }

    @Override
    public long getDriveID() {
        return driveID;
    }

    @Override
    public long getUserID() {
        return userID;
    }

    @Override
    public long getDate() {
        return date;
    }

    @Override
    public long getFrom() {
        return from;
    }

    @Override
    public long getTo() {
        return to;
    }

    @Override
    public int getVacantPlaces() {
        return vacantPlaces;
    }

    @Override
    public int getUsersNumber() {
        return joinedUsers.size();
    }

    @Override
    public Set<Long> getJoinedUsers() {
        return Collections.unmodifiableSet(joinedUsers);
    }

}

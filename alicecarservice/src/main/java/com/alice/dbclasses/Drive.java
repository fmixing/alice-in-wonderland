package com.alice.dbclasses;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Drive implements Serializable {

    /**
     * Drive's ID
     */
    private long driveID;

    /**
     * Who posted a drive
     */
    private long userID;

    /**
     * Starting point
     */
    private long from;

    /**
     * Destination point
     */
    private long to;

    /**
     * Date of drive
     */
    private long date;

    /**
     * Count of vacant place
     */

    private int vacantPlaces;

    /**
     * Set of joined users IDs
     */
    private final Set<Long> joinedUsers;

    Drive(long driveID, long userID, long from, long to, long date, int vacantPlaces) {
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
     * @return true if the attempt was successful
     */
    public boolean addUser(long joinedUserID) {
        synchronized (joinedUsers) {
            if (!(joinedUsers.size() < vacantPlaces)) {
                return false;
            }
            if (!joinedUsers.add(joinedUserID)){
                return false;
            }
        }
        return true;
    }

    /**
     * @return a copy of the current drive
     */
    public Drive cloneDrive() {
        Drive drive = new Drive(this.driveID, this.userID, this.from, this.to, this.date, this.vacantPlaces);
        synchronized (joinedUsers) {
            for (Long userID : joinedUsers) {
                drive.addUser(userID);
            }
        }
        return drive;
    }

    public long getDriveID() {
        return driveID;
    }

    public long getUserID() {
        return userID;
    }

    public long getDate() {
        return date;
    }

    public long getFrom() {
        return from;
    }

    public long getTo() {
        return to;
    }

    public int getVacantPlaces() {
        return vacantPlaces;
    }

    public int getUsersNumber() {
        return joinedUsers.size();
    }

    public Set<Long> getJoinedUsers() {
        return joinedUsers;
    }

}

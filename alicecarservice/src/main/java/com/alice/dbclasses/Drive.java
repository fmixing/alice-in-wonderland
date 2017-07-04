package com.alice.dbclasses;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class Drive implements Serializable {

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
     * Count of current joined users
     */
    private int usersNumber;

    /**
     * Set of joined users IDs
     */
    private Set<Long> joinedUsers;


    Drive(long userID, long from, long to, long date, int vacantPlaces) {
        this.userID = userID;
        this.from = from;
        this.to = to;
        this.date = date;
        this.vacantPlaces = vacantPlaces;
        usersNumber = 0;
        joinedUsers = new HashSet<>();

    }

    /**
     * Adds a user to a drive
     * @return true if the attempt was successful
     */
    public boolean addUser(long joinedUserID) {
        synchronized (this) {
            if (!(usersNumber < vacantPlaces)) {
                return false;
            }
            if (!joinedUsers.add(joinedUserID)){
                return false;
            }
            usersNumber++;
        }
        return true;
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
        return usersNumber;
    }

    public Set<Long> getJoinedUsers() {
        return joinedUsers;
    }

}

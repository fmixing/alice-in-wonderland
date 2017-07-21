package com.test.drive;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Drive implements Serializable {

    @Id
    @Column(name = "drive_id")
    private long id;

    private long userID;

    private long fromTown;

    private long toTown;

    private long date;

    private int vacantPlaces;

    @ManyToMany
    private Set<User> joinedUsers = new HashSet<>();

    public Drive() {}


    public Drive(long driveID, long userID, long from, long to, long date, int vacantPlaces) {
        this.id = driveID;
        this.userID = userID;
        this.fromTown = from;
        this.toTown = to;
        this.date = date;
        this.vacantPlaces = vacantPlaces;
    }

    public long getDriveID() {
        return id;
    }

    public void setDriveID(long driveID) {
        this.id = driveID;
    }

    public long getUserID() {
        return userID;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public long getFrom() {
        return fromTown;
    }

    public void setFrom(long from) {
        this.fromTown = from;
    }

    public long getTo() {
        return toTown;
    }

    public void setTo(long to) {
        this.toTown = to;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public int getVacantPlaces() {
        return vacantPlaces;
    }

    public void setVacantPlaces(int vacantPlaces) {
        this.vacantPlaces = vacantPlaces;
    }

    public Set<User> getJoinedUsers() {
        return joinedUsers;
    }

    public void setJoinedUsers(Set<User> joinedUsers) {
        this.joinedUsers = joinedUsers;
    }
}

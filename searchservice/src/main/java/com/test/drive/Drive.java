package com.test.drive;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
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

    // delete me?
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Drive drive = (Drive) o;
        return id == drive.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Drive{");
        sb.append("id=").append(id);
        sb.append(", userID=").append(userID);
        sb.append(", fromTown=").append(fromTown);
        sb.append(", toTown=").append(toTown);
        sb.append(", date=").append(date);
        sb.append(", vacantPlaces=").append(vacantPlaces);
        sb.append(", joinedUsers=").append(joinedUsers);
        sb.append('}');
        return sb.toString();
    }
}

package com.test.clientclasses;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Long userID;

    private Set<Long> postedDrives;

    private Set<Long> joinedDrives;

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Set<Long> getPostedDrives() {
        return postedDrives;
    }

    public void setPostedDrives(Set<Long> postedDrives) {
        this.postedDrives = postedDrives;
    }

    public Set<Long> getJoinedDrives() {
        return joinedDrives;
    }

    public void setJoinedDrives(Set<Long> joinedDrives) {
        this.joinedDrives = joinedDrives;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("User{");
        sb.append("userID=").append(userID);
        sb.append(", postedDrives=").append(postedDrives);
        sb.append(", joinedDrives=").append(joinedDrives);
        sb.append('}');
        return sb.toString();
    }
}

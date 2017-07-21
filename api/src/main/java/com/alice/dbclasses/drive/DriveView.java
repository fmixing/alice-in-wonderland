package com.alice.dbclasses.drive;

import java.util.Set;

public interface DriveView {

    long getDriveID();

    long getUserID();

    long getDate();

    long getFrom();

    long getTo();

    int getVacantPlaces();

    int getUsersNumber();

    Set<Long> getJoinedUsers();
}

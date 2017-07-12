package com.alice.dbclasses.drive;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public interface DriveDAO {

    Optional<DriveView> getDriveByID(long ID);

    Optional<DriveView> modify(long ID, Function<Drive, Optional<Drive>> mapper);

    DriveView createDrive(long userID, long from, long to, long date, int vacantPlaces, Consumer<Drive> mapper);

    Collection<DriveView> getDrives();

    void putToCache(Drive drive);

}

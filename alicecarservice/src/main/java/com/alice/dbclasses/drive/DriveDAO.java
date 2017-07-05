package com.alice.dbclasses.drive;

import java.util.List;
import java.util.Optional;

public interface DriveDAO {

    Optional<Drive> getDriveByID(long ID);

    Drive putDrive(Drive drive);

    List<DriveView> getDrives();

}

package com.alice.dbclasses;

import java.util.List;
import java.util.Set;

public interface DriveDAO {

    Drive getDriveByID(long ID);

    Drive putDrive(Drive drive);

    List<Drive> getDrives();

}

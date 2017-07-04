package com.alice.dbclasses;

import java.util.List;
import java.util.Set;

public interface DriveDAO {

    public Drive getDriveByID(long ID);

    public Drive putDrive(Drive drive);

    public List<Drive> getDrives();

}

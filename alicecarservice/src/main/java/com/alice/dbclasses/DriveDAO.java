package com.alice.dbclasses;

import java.util.List;

public interface DriveDAO {

    public Drive getDriveByID(int ID);

    public boolean putDrive(int ID, Drive drive);

    public List<Drive> getDrivesByFromToDate(long from, long to, long date);
}

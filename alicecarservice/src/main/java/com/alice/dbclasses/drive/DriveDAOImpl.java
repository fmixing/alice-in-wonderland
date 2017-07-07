package com.alice.dbclasses.drive;

import com.alice.dao.AbstractDAO;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


@Component
public class DriveDAOImpl extends AbstractDAO<DriveView, Drive>
{

    /**
     * Maps drives IDs to {@code Drive}
     */
    private final Map<Long, Drive> drives;

    private final AtomicLong idSequence = new AtomicLong();

    public DriveDAOImpl() {
        drives = new ConcurrentHashMap<>();
    }


    @Override
    protected Optional<Drive> get(long ID)
    {
        return Optional.ofNullable(drives.get(ID));
    }


    @Override
    protected DriveView put(long ID, Drive value)
    {
        Objects.requireNonNull(value);
        drives.put(value.getDriveID(), value);
        return value;
    }


    @Override
    public Optional<DriveView> getViewByID(long ID)
    {
        return Optional.ofNullable(drives.get(ID));
    }


    @Override
    public Collection<DriveView> getAllViews()
    {
        return Collections.unmodifiableCollection(drives.values());
    }


    public DriveView addDrive(long userID, long from, long to, long date, int vacantPlaces)
    {
        long id = idSequence.incrementAndGet();
        Drive drive = new Drive(id, userID, from, to, date, vacantPlaces);
        drives.put(id, drive);
        return drive;
    }
}

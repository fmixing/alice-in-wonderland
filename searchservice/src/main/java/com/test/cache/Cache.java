package com.test.cache;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.query.Query;

import static com.googlecode.cqengine.query.QueryFactory.*;

import com.googlecode.cqengine.resultset.ResultSet;
import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.googlecode.cqengine.stream.StreamFactory.streamOf;

@Component
// нужно более приятное название :)
public class Cache {

    /**
     * Amount of days contained in cache starting the current day
     */
    private final int cacheAmountOfDays = 10;

    private final DriveRepository repository;

    // нафига статика?
    private static final Attribute<Drive, Long> FROM = attribute("from", Drive::getFrom);
    private static final Attribute<Drive, Long> TO = attribute("to", Drive::getTo);
    private static final Attribute<Drive, Long> DATE = attribute("date", Drive::getDate);
    private static final SimpleAttribute<Drive, Long> ID = attribute("id", Drive::getDriveID);

    private final IndexedCollection<Drive> drivesCache = new ConcurrentIndexedCollection<>(OnHeapPersistence.onPrimaryKey(ID));

    // меняется и читается в разных потоках
    private volatile long dateFrom;
    private volatile long dateTo;

    @Autowired
    public Cache(DriveRepository repository) {
        this.repository = repository;

        long delay = getMidnightDate(1).getTime() - System.currentTimeMillis();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::processUpdate, delay,
                24 * 60 * 60 * 1000, TimeUnit.MILLISECONDS);

        initCache();
    }

    /**
     * Searches all the drives that suit this parameters
     * @param from the starting point of drives
     * @param to the destination point of drives
     * @param dateFrom the lower bound of drives' dates
     * @param dateTo the upper bound of drives' dates
     * @return List of all drives that were found
     */
    public List<Drive> getDrives(long from, long to, long dateFrom, long dateTo) {

        Query<Drive> query = and(and(equal(FROM, from), equal(TO, to)), between(DATE, dateFrom, dateTo));

        ResultSet<Drive> drives = drivesCache.retrieve(query, queryOptions(orderBy(ascending(DATE))));

        return streamOf(drives).collect(Collectors.toList());
    }

    /**
     * Replaces all the drives to their new versions and adds new drives
     * {@link com.googlecode.cqengine.ConcurrentIndexedCollection update} deletes previous versions
     * of drives in {@code drives} and adds the new versions plus new drives
     */
    public void addDrives(List<Drive> drives) {
        drivesCache.update(drives, drives);
    }


    /**
     * Method that is invoked at start of application,
     * fills cache with drives which dates are greater than or equal to the current date
     * and less than or equal to the current date plus {@code cacheAmountOfDays} days
     */
    private void initCache() {
        dateFrom = getMidnightDate(0).getTime();
        dateTo = getMidnightDate(cacheAmountOfDays).getTime();

        List<Drive> drivesToAdd = repository.find(dateFrom, dateTo);

        drivesCache.addAll(drivesToAdd);
    }

    /**
     * Method that is invoked at every midnight,
     * updates {@code drivesCache} using method {@link com.googlecode.cqengine.ConcurrentIndexedCollection update}
     * that removes all elements which dates are less than the current date
     * and adds elements which dates are equal to the current date plus {@code cacheAmountOfDays} days
     */
    private void processUpdate() {

        Long newDateFrom = getMidnightDate(0).getTime();

        Query<Drive> query = lessThan(DATE, newDateFrom);

        ResultSet<Drive> drivesToRemove = drivesCache.retrieve(query);

        dateFrom = newDateFrom;

        Long newDateTo = getMidnightDate(cacheAmountOfDays).getTime();

        List<Drive> drivesToAdd = repository.find(newDateTo, newDateTo);

        drivesCache.update(drivesToRemove, drivesToAdd);

        dateTo = newDateTo;
    }

    /**
     * Returns Date object contains midnight time of the current day plus offset
     * @param offsetDays the amount of days which should be added to the current day date
     */
    private Date getMidnightDate(int offsetDays) {
        Calendar date = new GregorianCalendar();

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        date.add(Calendar.DAY_OF_MONTH, offsetDays);

        return date.getTime();
    }

    public long getDateFrom() {
        return dateFrom;
    }

    public long getDateTo() {
        return dateTo;
    }
}

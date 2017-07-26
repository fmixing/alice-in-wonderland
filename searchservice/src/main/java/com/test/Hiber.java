package com.test;

import com.test.cache.Cache;
import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import com.test.drive.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/hiber")
// пора его переименовать, нам же нужно пушить в мастер :)
public class Hiber {

    @Autowired
    private DriveRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private Cache cache;

    /**
     * @param from the required starting point of drives
     * @param to the required destination point of drives
     * @param dateFrom the left boundary of required time interval
     * @param dateTo the right boundary of required time interval
     * @return all drives which dates are greater than the date of invocation
     * and their parameters fit the parameters of function
     */
    @RequestMapping("/searchDrive")
    public @ResponseBody
    List<Long> searchDrive(
            @RequestParam(value="from", required=true) long from,
            @RequestParam(value="to", required=true) long to,
            @RequestParam(value="dateFrom", required=true) long dateFrom,
            @RequestParam(value="dateTo", required=true) long dateTo) {

        List<Long> drives = new ArrayList<>();

        long cacheDateFrom = cache.getDateFrom();
        long cacheDateTo = cache.getDateTo();

        Optional<TimeInterval> timeIntervalFromCache = intersectTimeIntervals(dateFrom, dateTo, cacheDateFrom, cacheDateTo);

        if (timeIntervalFromCache.isPresent())
            drives = cache.getDrives(from, to, timeIntervalFromCache.get().from, timeIntervalFromCache.get().to)
                    .stream().map(Drive::getDriveID).collect(Collectors.toList());

        Optional<TimeInterval> timeIntervalFromDB = subtractTimeIntervals(dateFrom, dateTo, cacheDateFrom, cacheDateTo);

        if (timeIntervalFromDB.isPresent()) {
            drives.addAll(repository.find(from, to, timeIntervalFromDB.get().from, timeIntervalFromDB.get().to)
                    .stream().map(Drive::getDriveID).collect(Collectors.toList()));
        }

        return drives;
    }


    /**
     * Gets the time interval to search in database
     * @return Optional object contains time interval that should be checked in database,
     * Optional.empty if a search time interval is a subset of the cache time interval
     */
    private Optional<TimeInterval> subtractTimeIntervals(long dateFrom, long dateTo, long cacheFrom, long cacheTo) {
        TimeInterval timeInterval = new TimeInterval(Math.max(dateFrom, cacheTo + 1), dateTo);

        if (timeInterval.from > timeInterval.to)
            return Optional.empty();
        return Optional.of(timeInterval);
    }

    /**
     * Gets the time interval to search in cache
     * @return Optional object contains time interval that should be checked in cache,
     * Optional.empty if the intersection of this time intervals is empty
     */
    private Optional<TimeInterval> intersectTimeIntervals(long dateFrom, long dateTo, long cacheDateFrom, long cacheDateTo) {
        TimeInterval timeInterval = new TimeInterval(Math.max(dateFrom, cacheDateFrom), Math.min(dateTo, cacheDateTo));

        if (timeInterval.from > timeInterval.to)
            return Optional.empty();
        return Optional.of(timeInterval);
    }

    // мне кажется этому классу стоило бы быть отдельным классом... наверняка кому-то ещё понядобится)
    private class TimeInterval {
        Long from;
        Long to;

        TimeInterval(long from, long to) {
            this.from = from;
            this.to = to;
        }
    }

}

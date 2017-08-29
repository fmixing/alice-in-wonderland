package com.test;

import com.alice.utils.CommonMetrics;
import com.codahale.metrics.*;
import com.codahale.metrics.Timer;
import com.test.cache.DrivesSearchCache;
import com.test.dbclasses.Drive;
import com.test.dbclasses.DriveRepository;
import com.test.dbclasses.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/hiber")
public class SearchService {

    @Autowired
    private DriveRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DrivesSearchCache drivesSearchCache;

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

        final Timer.Context context = CommonMetrics.getTimerContext(SearchService.class,"search-request");

        try {
            List<Long> drives = new ArrayList<>();

            long cacheDateFrom = drivesSearchCache.getDateFrom();
            long cacheDateTo = drivesSearchCache.getDateTo();

            Optional<TimeInterval> timeIntervalFromCache = intersectTimeIntervals(dateFrom, dateTo, cacheDateFrom, cacheDateTo);

            if (timeIntervalFromCache.isPresent())
                drives = drivesSearchCache.getDrives(from, to, timeIntervalFromCache.get().from, timeIntervalFromCache.get().to)
                        .stream().map(Drive::getDriveID).collect(Collectors.toList());

            Optional<TimeInterval> timeIntervalFromDB = subtractTimeIntervals(dateFrom, dateTo, cacheDateFrom, cacheDateTo);

            if (timeIntervalFromDB.isPresent()) {
                drives.addAll(repository.find(from, to, timeIntervalFromDB.get().from, timeIntervalFromDB.get().to)
                        .stream().map(Drive::getDriveID).collect(Collectors.toList()));
            }

            return drives;
        } finally {
            context.stop();
        }
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

    private class TimeInterval {
        Long from;
        Long to;

        TimeInterval(long from, long to) {
            this.from = from;
            this.to = to;
        }
    }

}

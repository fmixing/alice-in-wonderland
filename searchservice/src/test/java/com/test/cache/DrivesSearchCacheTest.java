package com.test.cache;

import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.test.dbclasses.Drive;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.googlecode.cqengine.query.QueryFactory.attribute;

/**
 * Created by alice on 25.07.17.
 */
public class DrivesSearchCacheTest {
    @Test
    public void name() throws Exception {
        final Attribute<Drive, Long> FROM = attribute("from", Drive::getFrom);
        final Attribute<Drive, Long> TO = attribute("to", Drive::getTo);
        final Attribute<Drive, Long> DATE = attribute("date", Drive::getDate);
        final SimpleAttribute<Drive, Long> ID = attribute("id", Drive::getDriveID);

        final IndexedCollection<Drive> drivesCache = new ConcurrentIndexedCollection<>();

        drivesCache.add(new Drive(1, 2, 1, 2, 1, 4));
        drivesCache.forEach(System.out::println);
        Drive d = new Drive(1, 2, 1, 2, 1, 3);
        drivesCache.update(Arrays.asList(d, new Drive(2, 2, 3, 4, 5, 6)),
                Collections.singletonList(d));
        drivesCache.forEach(System.out::println);
    }
}
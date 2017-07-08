package com.alice.dbclasses;

import com.alice.services.CitiesService;
import org.junit.Test;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.Assert.*;

public class CitiesServiceTest {

    private CitiesService citiesService = new CitiesService();

    @Test
    public void testExistingCities() {
        Optional<Long> spbID = citiesService.getCityID("Санкт-Петербург");
        assertTrue("SPb should have been added", spbID.isPresent());
        assertEquals("Wrong ID", 1L, (long)spbID.get());
        Optional<Long> mscID = citiesService.getCityID("Москва");
        assertTrue("Msc should have been added", mscID.isPresent());
        assertEquals("Wrong ID", 2L, (long)mscID.get());

        Optional<String> spb = citiesService.getCityName(1);
        assertTrue("SPb should have been added", spb.isPresent());
        assertEquals("Wrong name", "Санкт-Петербург", spb.get());
        Optional<String> msc = citiesService.getCityName(2);
        assertTrue("Msc should have been added", msc.isPresent());
        assertEquals("Wrong name", "Москва", msc.get());
    }

    @Test
    public void testNotExistingCity() {
        Optional<Long> vlgID = citiesService.getCityID("Волгоград");
        assertFalse("This city should not have been added", vlgID.isPresent());
        Optional<String> city = citiesService.getCityName(3);
        assertFalse("This city should not have been added", city.isPresent());
    }

    @Test
    public void testGettingCitiesNames() {
        List<String> names = citiesService.getCitiesNames();
        long seenCount = Stream.of("Санкт-Петербург", "Москва").filter(names::contains).count();
        assertEquals("Number of cities",2, seenCount);
    }
}

package com.alice.dbclasses;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class Cities {

    /**
     * Maps city name to its ID
     */
    private Map<String, Long> cities;

    public Cities() {
        cities = new ConcurrentHashMap<>();
        cities.put("Санкт-Петербург", (long) 1);
        cities.put("Москва", (long) 2);
    }

    /**
     * @param cityName name of a city
     * @return ID of this city, -1 if this city doesn't exist in DB
     */
    public long getCityID(String cityName) {
        if (cities.containsKey(cityName)) {
            return cities.get(cityName);
        }
        return -1;
    }

    /**
     * @return List of all cities which are contained in DB
     */
    public List<String> getCitiesNames() {
        return new ArrayList<>(cities.keySet());
    }
}

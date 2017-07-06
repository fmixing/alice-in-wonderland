package com.alice.Services;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CitiesService {

    /**
     * Maps city name to its ID
     */
    private final BiMap<String, Long> cities;

    public CitiesService() {
        cities = Maps.synchronizedBiMap(HashBiMap.create());
        cities.put("Санкт-Петербург", 1L);
        cities.put("Москва", 2L);
    }

    /**
     * @param cityName name of a city
     * @return an {@code Optional} object which contains ID of this city if this city exists,
     * an empty Optional otherwise
     */
    public Optional<Long> getCityID(String cityName) {
        return Optional.ofNullable(cities.get(cityName));
    }

    /**
     * @return List of all cities which are contained in DB
     */
    // если вернуть Collection<String> то можно не создавать нового массива
    // по сути тут порядок не важен
    public Collection<String> getCitiesNames() {
        return Collections.unmodifiableCollection(cities.keySet());
        // return new ArrayList<>(cities.keySet());
    }


    /**
     * @param ID which
     * @return an {@code Optional} object which contains city name if the city with this ID exists,
     * an empty Optional otherwise
     */
    public Optional<String> getCityName(long ID) {
        return Optional.ofNullable(cities.inverse().get(ID));
    }
}

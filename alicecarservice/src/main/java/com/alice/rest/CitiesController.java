package com.alice.rest;

import com.alice.services.CitiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Map;

@Controller
@RequestMapping("/api/cities")
public class CitiesController {

    @Autowired
    private CitiesService citiesService;

    @RequestMapping("/get_all")
    public @ResponseBody
    Map<String, Long> getAllCities() {
        return citiesService.getCitiesWithId();
    }

}

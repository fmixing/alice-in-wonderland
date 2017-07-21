package com.test;

import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import com.test.drive.User;
import com.test.drive.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;


@Controller
@RequestMapping("/hiber")
public class Hiber {
    @Autowired
    DriveRepository repository;

    @Autowired
    UserRepository userRepository;

    @RequestMapping("/add")
    public String addDrive(@RequestParam(value="driveID", required=true) Long driveID) {

        Drive drive = new Drive(driveID, 2L, 1L, 2L, System.currentTimeMillis(), 100);
        User user = new User(2);
        drive.getJoinedUsers().add(user);

        userRepository.save(user);

        repository.save(drive);

        return "Successful";
    }

    @RequestMapping("/searchDrive")
    public @ResponseBody
    List<Long> searchDrive(
            @RequestParam(value="from", required=true) long from,
            @RequestParam(value="to", required=true) long to,
            @RequestParam(value="dateFrom", required=true) long dateFrom,
            @RequestParam(value="dateTo", required=true) long dateTo) {

        return repository.find(from, to, dateFrom, dateTo);
    }


    @RequestMapping("/add_user")
    public String addUserToDrive(
            @RequestParam(value="driveID", required=true) Long driveID,
            @RequestParam(value="userID", required=true) Long userID) {

        Drive drive = repository.getOne(driveID);

        User user = new User(userID);

        drive.getJoinedUsers().add(user);

        userRepository.save(user);

        repository.save(drive);
        return "Successful";
    }

}

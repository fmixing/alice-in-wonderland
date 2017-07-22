package com.test.db;

import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import com.test.drive.User;
import com.test.drive.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class UpdateDB {

    @Autowired
    DriveRepository driveRepository;

    @Autowired
    UserRepository userRepository;

    @Transactional(rollbackFor = Exception.class)
    public void updateDrives(List<com.alice.dbclasses.drive.Drive> drives) {
        List<User> dbUsers = new ArrayList<>();

        // пожалуйста, Алиса, не перемешивай функциональный стиль и императивный
        // функциональный куда круче, на мой взгляд
        List<User> users = drives.stream().map(com.alice.dbclasses.drive.Drive::getJoinedUsers).flatMap(Set::stream)
                .map(User::new).collect(Collectors.toList());

        drives.forEach(drive -> dbUsers.addAll(
                drive.getJoinedUsers().stream().map(User::new).collect(Collectors.toList())));
        userRepository.save(dbUsers);

        List<Drive> dbDrives = new ArrayList<>();

        dbDrives.addAll(drives.stream().
                map(drive -> {
                    // тут хорошо зайдет функция из драйва в драйв
                    // a-ля .map(this::convertDrive).collect...
                    Drive dbDrive = new Drive(drive.getDriveID(), drive.getUserID(),
                            drive.getFrom(), drive.getTo(), drive.getDate(), drive.getVacantPlaces());
                    dbDrive.setJoinedUsers(drive.getJoinedUsers().stream().map(User::new).collect(Collectors.toSet()));
                    return dbDrive;
                }).collect(Collectors.toList()));
        driveRepository.save(dbDrives);
    }

}

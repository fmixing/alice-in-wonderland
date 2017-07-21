package com.test;

import com.test.drive.Drive;
import com.test.drive.DriveRepository;
import com.test.drive.User;
import com.test.drive.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


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

//    @RequestMapping("/add")
//    public String addDrive(@RequestParam(value="driveID", required=true) Long driveID) {
//
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mnf-pu");
//
//        System.err.println(emf);
//
//        EntityManager em = emf.createEntityManager();
//        em.setFlushMode(FlushModeType.COMMIT);
//
//        em.getTransaction().begin();
//
//        Drive drive = new Drive(driveID, 1L, 1L, 2L, System.currentTimeMillis(), 100);
//
//        em.persist(drive);
//
//        em.getTransaction().commit();
//
//
//        return "Successful";
//    }


//    @RequestMapping("/add_user")
//    public String addUserToDrive(@RequestParam(value="driveID", required=true) Long driveID,
//                           @RequestParam(value="driveID", required=true) Long userID) {
//        EntityManagerFactory emf = Persistence.createEntityManagerFactory("mnf-pu");
//        EntityManager em = emf.createEntityManager();
//        em.setFlushMode(FlushModeType.COMMIT);
//
//        em.getTransaction().begin();
//
//        String s = "from Drive where id='"+driveID+"'";
//        Drive drive = em.createQuery(s, Drive.class).getSingleResult();
//
//        Set<Long> users = drive.
//
//        return "Successful";
//    }

}

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class TestConcurrent {


    private static final Logger logger = LoggerFactory.getLogger(TestConcurrent.class);

    private RestTemplate restTemplate = new RestTemplate();


    private List<ClientThread> clientThreads;


    public TestConcurrent(int count) {

        clientThreads = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            clientThreads.add(new ClientThread("test" + (i + 1)));
        }

//        clientThreads.forEach(v -> v.setDaemon(true));
    }

    public void run() {
        for (ClientThread clientThread : clientThreads) {
            clientThread.start();
        }
        clientThreads.forEach(v -> {
            try {
                v.join();
            } catch (InterruptedException e) {

            }
        });
    }

    private class ClientThread extends Thread {

        UriComponentsBuilder builder;
        Long timeBefore;
        Long timeAfter;


        ClientThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                Random random = new Random();

                int logRand = random.nextInt(2000);
                String log = "a" + logRand;

                ResultUser user = createUser(log);

                if (user.hasResult()) {
                    ResultDrive drive = createDrive(user.getJsonUser().getUserID());

                    List<Drive> drives = getDrives();

                    Drive driveJoin = drives.get(random.nextInt(drives.size()));

                    joinDrive(user.getJsonUser().getUserID(), driveJoin.driveID);

                    getDrive(driveJoin.driveID);
                    getUser(user.getJsonUser().getUserID());

                    getUsers();

                } else {

                    List<Drive> drives = getDrives();

                    Drive drive = drives.get(random.nextInt(drives.size()));

                    getDrive(drive.driveID);

                    getUsers();
                }

//                int time = random.nextInt(5000);
                int time = 3000;
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        private ResultUser createUser(String log) {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/create_JSON")
                    .queryParam("login", log)
                    .queryParam("password", "bbb");

            ResultUser resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);

            timeAfter = System.currentTimeMillis();

            if (resultUser.hasMessage()) {
                logger.info("Request: create user, got an error : '{}', time spent on request = {}", resultUser.getMessage(), (timeAfter-timeBefore));
            } else {
                logger.info("Request: create user, got a user : '{}', time spent on request = {}", resultUser.getJsonUser(), (timeAfter-timeBefore));
            }

            return resultUser;
        }

        private ResultUser getUser(Long userID) {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_JSON")
                    .queryParam("id", userID);

            ResultUser resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);

            timeAfter = System.currentTimeMillis();

            if (resultUser.hasMessage()) {
                logger.info("Request: get user, got an error : '{}', time spent on request = {}", resultUser.getMessage(), (timeAfter-timeBefore));
            } else {
                logger.info("Request: get user, got a user : '{}', time spent on request = {}",
                        resultUser.getJsonUser(), (timeAfter-timeBefore));
            }

            return resultUser;
        }

        private List<User> getUsers() {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

            ResponseEntity<User[]> responseEntity = restTemplate.getForEntity(builder.build().toString(), User[].class);

            List<User> users = Arrays.asList(responseEntity.getBody());
            timeAfter = System.currentTimeMillis();

            logger.info("Request: get all users, time spent on request = {}", (timeAfter-timeBefore));

            return users;
        }



        private ResultDrive getDrive(Long driveID) {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_JSON")
                    .queryParam("driveID", driveID);

            ResultDrive resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);

            timeAfter = System.currentTimeMillis();

            if (resultDrive.hasMessage()) {
                logger.info("Request: get drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter-timeBefore));
            } else {
                logger.info("Request: get drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter-timeBefore));
            }

            return resultDrive;
        }

        private ResultDrive joinDrive(Long userID, Long driveID) {
            timeBefore = System.currentTimeMillis();
            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/join_drive_JSON")
                    .queryParam("userID", userID)
                    .queryParam("driveID", driveID);

            ResultDrive resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
            timeAfter = System.currentTimeMillis();

            if (resultDrive.hasMessage()) {
                logger.info("Request: join drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter-timeBefore));
            } else {
                logger.info("Request: join drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter-timeBefore));
            }

            return resultDrive;
        }

        private List<Drive> getDrives() {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_all_JSON");

            ResponseEntity<Drive[]> responseEntity = restTemplate.getForEntity(builder.build().toString(), Drive[].class);

            List<Drive> drives = Arrays.asList(responseEntity.getBody());
            timeAfter = System.currentTimeMillis();

            logger.info("Request: get all drives, time spent on request = {}", (timeAfter-timeBefore));

            return drives;
        }

        private ResultDrive createDrive(Long userID) {
            timeBefore = System.currentTimeMillis();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/create_JSON")
                    .queryParam("userID", userID)
                    .queryParam("from", 1)
                    .queryParam("to", 2)
                    .queryParam("vacantPlaces", 2)
                    .queryParam("date", System.currentTimeMillis());

            ResultDrive resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);

            timeAfter = System.currentTimeMillis();

            if (resultDrive.hasMessage()) {
                logger.info("Request: create drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter-timeBefore));
            } else {
                logger.info("Request: create drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter-timeBefore));
            }

            return resultDrive;
        }


    }

}

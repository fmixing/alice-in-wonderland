package com.test.tests;

import com.test.clientclasses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class TestConcurrent {


    private static final Logger logger = LoggerFactory.getLogger(TestConcurrent.class);
    private static final Logger loggerError = LoggerFactory.getLogger("TestError");

    private List<Long> drivesIDs;

    private RestTemplate restTemplate = new RestTemplate();


    private List<ClientThread> clientThreads;


    public TestConcurrent(int count) {

        clientThreads = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            clientThreads.add(new ClientThread("test" + (i + 1)));
        }

        drivesIDs = Collections.synchronizedList(new ArrayList<>());
        Objects.requireNonNull(drivesIDs);
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

                int logRand = random.nextInt(1000);
                String login = "a" + logRand;

                ResultUser user;
                while (true) {
                    user = createUser(login);

                    if (!(user.hasMessage() && user.getMessage().startsWith("Some problems with database occurred")))
                        break;
                }

                if (user.hasMessage() && user.getMessage().startsWith("User with login")) {
                    Long userID = getUserID(login);
                    user = getUser(userID);
                }

                Objects.requireNonNull(user.getJsonUser().getUserID());
                createDrive(user.getJsonUser().getUserID());

                Long driveIDJoin;

                int rand = random.nextInt(drivesIDs.size());

                driveIDJoin = drivesIDs.get(rand);

                Objects.requireNonNull(driveIDJoin);

                joinDrive(user.getJsonUser().getUserID(), driveIDJoin);

                getDrive(driveIDJoin);

                getUser(user.getJsonUser().getUserID());
//                getUsers();


//                int time = random.nextInt(5000);
//                int time = 10;
//                try {
//                    sleep(time);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
        }

        /**
         * This method should only be used if a user with this login already exists
         */
        private Long getUserID(String login) {

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_user_id")
                    .queryParam("login", login)
                    .queryParam("password", "bbb");

            ResultID resultID;
            Long ID;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultID = restTemplate.getForObject(builder.build().toString(), ResultID.class);
                    if (resultID.hasResult()) {
                        ID = resultID.getJsonID();
                        break;
                    }
                    else {
                        loggerError.error("Request: get user ID, got an error : '{}'", resultID.getMessage());
                    }
                } catch (Exception e) {
                    loggerError.error("Request: get user ID, got an exception : '{}'", e.getMessage());
                }
            }

            timeAfter = System.currentTimeMillis();
            Objects.requireNonNull(ID);

            logger.info("Request: get user ID, got an ID : '{}', time spent on request = {}", ID, (timeAfter - timeBefore));

            return ID;
        }

        private ResultUser createUser(String log) {
            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/create_JSON")
                    .queryParam("login", log)
                    .queryParam("password", "bbb");

            ResultUser resultUser;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: create user, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultUser);
            timeAfter = System.currentTimeMillis();

            if (resultUser.hasMessage()) {
                logger.info("Request: create user, got an error : '{}', time spent on request = {}", resultUser.getMessage(), (timeAfter - timeBefore));
            } else {
                logger.info("Request: create user, got a user : '{}', time spent on request = {}", resultUser.getJsonUser(), (timeAfter - timeBefore));
            }

            return resultUser;
        }

        private ResultUser getUser(Long userID) {

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_JSON")
                    .queryParam("id", userID);

            ResultUser resultUser;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: get user, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultUser);
            timeAfter = System.currentTimeMillis();

            if (resultUser.hasMessage()) {
                logger.info("Request: get user, got an error : '{}', time spent on request = {}", resultUser.getMessage(), (timeAfter - timeBefore));
            } else {
                logger.info("Request: get user, got a user : '{}', time spent on request = {}",
                        resultUser.getJsonUser(), (timeAfter - timeBefore));
            }

            return resultUser;
        }

        private List<User> getUsers() {

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

            ResponseEntity<User[]> responseEntity;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    responseEntity = restTemplate.getForEntity(builder.build().toString(), User[].class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: get all users, got an exception : '{}'", e.getMessage());
                }
            }

            List<User> users = Arrays.asList(responseEntity.getBody());
            timeAfter = System.currentTimeMillis();

            logger.info("Request: get all users, time spent on request = {}", (timeAfter - timeBefore));

            return users;
        }


        private ResultDrive getDrive(Long driveID) {

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_JSON")
                    .queryParam("driveID", driveID);

            ResultDrive resultDrive;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: get drive, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultDrive);
            timeAfter = System.currentTimeMillis();

            if (resultDrive.hasMessage()) {
                logger.info("Request: get drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter - timeBefore));
            } else {
                logger.info("Request: get drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter - timeBefore));
            }

            return resultDrive;
        }

        private ResultDrive joinDrive(Long userID, Long driveID) {

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/join_drive_JSON")
                    .queryParam("userID", userID)
                    .queryParam("driveID", driveID);

            ResultDrive resultDrive;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: join drive, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultDrive);
            timeAfter = System.currentTimeMillis();

            if (resultDrive.hasMessage()) {
                logger.info("Request: join drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter - timeBefore));
            } else {
                logger.info("Request: join drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter - timeBefore));
            }

            return resultDrive;
        }

        private List<Drive> getDrives() {


            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_all_JSON");

            ResponseEntity<Drive[]> responseEntity;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    responseEntity = restTemplate.getForEntity(builder.build().toString(), Drive[].class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: get all drives, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(responseEntity);
            List<Drive> drives = Arrays.asList(responseEntity.getBody());
            timeAfter = System.currentTimeMillis();

            logger.info("Request: get all drives, time spent on request = {}", (timeAfter - timeBefore));

            return drives;
        }

        private ResultDrive createDrive(Long userID) {

            GregorianCalendar calendar = new GregorianCalendar();
            int year = randBetween(2017, 2018);
            calendar.set(GregorianCalendar.YEAR, year);
            int dayOfYear = randBetween(1, calendar.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
            calendar.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
            long date = calendar.getTime().getTime();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/create_JSON")
                    .queryParam("userID", userID)
                    .queryParam("from", 1)
                    .queryParam("to", 2)
                    .queryParam("vacantPlaces", 2)
                    .queryParam("date", date);

            ResultDrive resultDrive;

            while (true) {
                try {
                    timeBefore = System.currentTimeMillis();
                    resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                    break;
                } catch (Exception e) {
                    loggerError.error("Request: create drive, got an exception : '{}'", e.getMessage());
                }
            }

            timeAfter = System.currentTimeMillis();
            Objects.requireNonNull(resultDrive);

            if (resultDrive.hasMessage()) {
                logger.info("Request: create drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage(), (timeAfter - timeBefore));
            } else {
                logger.info("Request: create drive, got a drive : '{}', time spent on request = {}",
                        resultDrive.getJsonDrive(), (timeAfter - timeBefore));
                drivesIDs.add(resultDrive.getJsonDrive().getDriveID());
            }

            return resultDrive;
        }

        private int randBetween(int start, int end) {
            return start + (int) Math.round(Math.random() * (end - start));
        }


    }

}

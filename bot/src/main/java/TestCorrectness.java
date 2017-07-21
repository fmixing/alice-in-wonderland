import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

public class TestCorrectness {

    private static final Logger logger = LoggerFactory.getLogger(TestCorrectness.class);

    private RestTemplate restTemplate = new RestTemplate();

    private List<ClientThread> clientThreads;

    private List<Drive> drives;

    public TestCorrectness() {

        createDataInDB();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

        ResponseEntity<User[]> responseEntityUsers = restTemplate.getForEntity(builder.build().toString(), User[].class);

        List<User> users = Arrays.asList(responseEntityUsers.getBody());

        builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_all_JSON");

        ResponseEntity<Drive[]> responseEntityDrives = restTemplate.getForEntity(builder.build().toString(), Drive[].class);

        drives = Collections.synchronizedList(Arrays.asList(responseEntityDrives.getBody()));

        int count = users.size();

        clientThreads = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            clientThreads.add(new ClientThread("testCorr" + (i + 1), users.get(i).getUserID()));
        }

        clientThreads.forEach(Thread::start);

        clientThreads.forEach(v -> {
            try {
                v.join();
            } catch (InterruptedException e) {

            }
        });


        builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

        responseEntityUsers = restTemplate.getForEntity(builder.build().toString(), User[].class);

        users = Arrays.asList(responseEntityUsers.getBody());

        builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_all_JSON");

        responseEntityDrives = restTemplate.getForEntity(builder.build().toString(), Drive[].class);

        drives = Collections.synchronizedList(Arrays.asList(responseEntityDrives.getBody()));

        ConcurrentMap<Long, Drive> drivesMap = drives.stream().collect(Collectors.toConcurrentMap(Drive::getDriveID, Function.identity()));
        ConcurrentMap<Long, User> usersMap = users.stream().collect(Collectors.toConcurrentMap(User::getUserID, Function.identity()));


        Integer totalAmountJoinedDrives = users.stream().map(User::getJoinedDrives).mapToInt(Set::size).sum();
        Integer totalAmountDrives = drives.stream().map(Drive::getJoinedUsers).mapToInt(Set::size).sum();



        if (!totalAmountJoinedDrives.equals(totalAmountDrives)) {
            System.out.println("Total amount users' joined drives: " + totalAmountJoinedDrives + ", total amount of successful joining:  " + totalAmountDrives);
        }

        for (User user : users) {
            for (Long driveID : user.getJoinedDrives()) {
                if (!drivesMap.get(driveID).joinedUsers.contains(user.userID))
                    throw new RuntimeException("UserID " +user.userID + ", driveID " + driveID);
            }
        }

        for (Drive drive : drives) {
            for (Long userID : drive.getJoinedUsers()) {
                if (!usersMap.get(userID).joinedDrives.contains(drive.driveID))
                    throw new RuntimeException("DriveID " +drive.driveID + ", userID " + userID);
            }
            if (drive.joinedUsers.size() > drive.vacantPlaces)
                throw new RuntimeException("DriveID " + drive.driveID);
        }



        boolean posted = clientThreads.stream().map(ClientThread::isPosted).allMatch(e -> e.equals(true));

        if (!posted) {
            System.out.println("Users who didn't post a drive: ");
            for (ClientThread clientThread : clientThreads) {
                if (!clientThread.isPosted()) {
                    System.out.print(clientThread.getUserID() + " ");

                    users.stream().filter(v -> v.getUserID().equals(clientThread.getUserID())).forEach(System.out::println);
                }
            }
        }

    }

    private void createDataInDB() {
        for (int i = 0; i < 200; i++) {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/create_JSON")
                    .queryParam("login", "aaa" + i)
                    .queryParam("password", "bbb");

            ResultUser resultUser = null;

            boolean sent = false;

            while (!sent) {
                try {
                    resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);

                    if (resultUser.hasMessage()) {
                        logger.info("Request: create user, got an error : '{}', time spent on request = {}", resultUser.getMessage());
                    } else {
                        logger.info("Request: create user, got a user : '{}', time spent on request = {}", resultUser.getJsonUser());
                    }
                    sent = true;
                } catch (Exception e) {
                    logger.error("Request: join drive, got an exception : '{}'", e.getMessage());
                }
            }

            GregorianCalendar calendar = new GregorianCalendar();
            int year = randBetween(2017, 2018);
            calendar.set(GregorianCalendar.YEAR, year);
            int dayOfYear = randBetween(1, calendar.getActualMaximum(GregorianCalendar.DAY_OF_YEAR));
            calendar.set(GregorianCalendar.DAY_OF_YEAR, dayOfYear);
            long date = calendar.getTime().getTime();

            builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/create_JSON")
                    .queryParam("userID", resultUser.getJsonUser().getUserID())
                    .queryParam("from", 1)
                    .queryParam("to", 2)
                    .queryParam("vacantPlaces", 2)
                    .queryParam("date", date);

            sent = false;

            while (!sent) {

                try {
                    ResultDrive resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);

                    if (resultDrive.hasMessage()) {
                        logger.info("Request: create drive, got an error : '{}', time spent on request = {}", resultDrive.getMessage());
                    } else {
                        logger.info("Request: create drive, got a drive : '{}', time spent on request = {}",
                                resultDrive.getJsonDrive());
                    }
                    sent = true;
                } catch (Exception e) {
                    logger.error("Request: join drive, got an exception : '{}'", e.getMessage());
                }
            }
        }
    }


    private int randBetween(int start, int end) {
        return start + (int)Math.round(Math.random() * (end - start));
    }


    private class ClientThread extends Thread {

        private Long userID;
        private Drive drive;
        private UriComponentsBuilder builder;

        private volatile boolean posted = false;


        public ClientThread(String name, Long userID) {
            super(name);
            this.userID = userID;
        }

        @Override
        public void run() {

            int rand = new Random().nextInt(drives.size());



            for (int i = 0; i < drives.size(); i++) {

                boolean sent = false;

                drive = drives.get((rand + i) % drives.size());

                builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/join_drive_JSON")
                        .queryParam("userID", userID)
                        .queryParam("driveID", drive.driveID);

                logger.info("Request: join drive, url : '{}'", builder.build().toString());

                while (!sent) {

                    ResultDrive resultDrive = null;
                    try {
                        resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                        if (resultDrive.hasResult()) {
                            logger.info("Request: join drive, got a drive : '{}'", resultDrive.getJsonDrive());
                        } else {
                            if (resultDrive.getMessage().equals("User can't join to a drive which they created")) {
                                posted = true;
                            }
                            logger.info("Request: join drive, got an error : '{}'", resultDrive.getMessage());
                        }
                        sent = true;
                    } catch (Exception e) {
                        logger.error("Request: join drive, got an exception : '{}'", e.getMessage());
                    }
                }

            }

        }

        public boolean isPosted() {
            return posted;
        }

        public Long getUserID() {
            return userID;
        }
    }


}

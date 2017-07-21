import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
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

    private class ClientThread extends Thread {

        private Long userID;
        private Drive drive;
        private UriComponentsBuilder builder;

        private boolean posted = false;


        public ClientThread(String name, Long userID) {
            super(name);
            this.userID = userID;
        }

        @Override
        public void run() {

            int rand = new Random().nextInt(drives.size());

            for (int i = 0; i < drives.size(); i++) {

                drive = drives.get((rand + i) % drives.size());

                builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/join_drive_JSON")
                        .queryParam("userID", userID)
                        .queryParam("driveID", drive.driveID);

                ResultDrive resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);

                if (resultDrive.hasResult()) {
                    logger.info("Request: join drive, got a drive : '{}'", resultDrive.getJsonDrive());
                }
                else {
                    if (resultDrive.getMessage().equals("User can't join to a drive which they created")) {
                        posted = true;
                    }
                    logger.info("Request: join drive, got an error : '{}'", resultDrive.getMessage());
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

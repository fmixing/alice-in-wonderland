import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

public class TestCorrectness {

   // private static final Logger logger = LoggerFactory.getLogger(TestCorrectness.class);

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

        Long totalAmountThreads = clientThreads.stream().map(ClientThread::getSuccessful).count();
        Long totalAmountDrives = drives.stream().map(Drive::getJoinedUsers).count();
        if (!totalAmountThreads.equals(totalAmountDrives)) {
            System.out.println("Total amount of joined users: " + totalAmountThreads + " total amount of successful joining:  " + totalAmountDrives);
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

        private int successful = 0;
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
                    successful++;
      //              logger.info("Request: join drive, got a drive : '{}'", resultDrive.getJsonDrive());
                }
                else {
                    if (resultDrive.getMessage().equals("User can't join to a drive which they created")) {
                        posted = true;
                    }
        //            logger.info("Request: join drive, got an error : '{}'", resultDrive.getMessage());
                }
            }

        }

        public int getSuccessful() {
            return successful;
        }

        public boolean isPosted() {
            return posted;
        }

        public Long getUserID() {
            return userID;
        }
    }


}

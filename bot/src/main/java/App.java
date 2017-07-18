import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jdk.nashorn.internal.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class App {

    private static final Logger logger = LoggerFactory.getLogger(App.class);

    private static RestTemplate restTemplate = new RestTemplate();
    static ObjectMapper mapper = new ObjectMapper();


    public static void main(String[] args) {

//        ClientThread clientThread = new ClientThread("1");
//
//        clientThread.setDaemon(true);
//
//        clientThread.run();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

        ResponseEntity<User[]> responseEntity = restTemplate.getForEntity(builder.build().toString(), User[].class);
        User[] objects = responseEntity.getBody();

        List<User> users = Arrays.asList(objects);

        users.forEach(System.out::println);

    }

    private static class ClientThread extends Thread {

        String name;

        ClientThread(String name) {
            super(name);
            this.name = name;
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {

                int logRand = new Random().nextInt(2000);
                String log = "a" + logRand;

                Long timeBefore = System.currentTimeMillis();

                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/create_JSON")
                        .queryParam("login", log)
                        .queryParam("password", "bbb");

                ResultUser resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);

                Long timeAfter = System.currentTimeMillis();

                if (resultUser.hasMessage()) {
                    logger.info("Thread-{} got an error while creating user:'{}', time spent on request={}", name, resultUser.getMessage(), (timeAfter-timeBefore));
                } else {
                    logger.info("Thread-{} got a user:'{}', time spent on request={}", name, resultUser.getJsonUser().toString(), (timeAfter-timeBefore));
                }

                int time = new Random().nextInt(2000);
                try {
                    sleep(time);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}

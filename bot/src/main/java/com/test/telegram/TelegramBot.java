package com.test.telegram;

import com.test.clientclasses.Drive;
import com.test.clientclasses.ResultDrive;
import com.test.clientclasses.ResultUser;
import com.test.clientclasses.User;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class TelegramBot extends TelegramLongPollingBot {

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    private final String token;

    private final String username;

    private BiMap<String, Long> cities = null;

    @Override
    public String getBotToken() {
        return token;
    }

    private RestTemplate restTemplate = new RestTemplate();

    @Override
    public String getBotUsername() {
        return username;
    }


    public TelegramBot(@Value("${bot.token}") String token, @Value("${bot.username}") String username) {
        this.token = token;
        this.username = username;
    }

    private void getCitiesMap() {
        while (true) {
            try {
                UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/cities/get_all");
                ResponseEntity<Map> responseEntityDrives = restTemplate.getForEntity(builder.build().toString(), Map.class);

                @SuppressWarnings("unchecked")
                Map<String, Integer> citiesMap = responseEntityDrives.getBody();

                Map<String, Long> collect = citiesMap.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> (long) e.getValue()));

                cities = Maps.synchronizedBiMap(HashBiMap.create(collect));
                break;
            } catch (RestClientException e) {
                logger.error("Request: get cities map, got an exception : '{}'", e.getMessage());
            }
        }
    }


    private String getAddDrive(Message message) {
        if (cities == null)
            getCitiesMap();
        String[] strings = message.getText().split(" ");
        String text;
        List<String> parsedMessage = new ArrayList<>();

        for (String s : strings) {
            parsedMessage.add(s.trim());
        }

        if (parsedMessage.size() != 8) {
            text = "Wrong amount of command args";
        } else if (!parsedMessage.get(1).matches("[0-9]+")) {
            text = "User ID should contain only digits";
        } else if (!cities.containsKey(parsedMessage.get(2)) || !cities.containsKey(parsedMessage.get(3))) {
            text = "Wrong name of city. To check the names send \"/cities\"";
        } else {
            long userID = Long.parseLong(parsedMessage.get(1));
            Long from = cities.get(parsedMessage.get(2));
            Long to = cities.get(parsedMessage.get(3));
            Calendar calendar = new GregorianCalendar(Integer.parseInt(parsedMessage.get(4)),
                    Integer.parseInt(parsedMessage.get(5)) - 1, Integer.parseInt(parsedMessage.get(6)));
            long date = calendar.getTime().getTime();
            int vacantPlaces = Integer.parseInt(parsedMessage.get(7));

            if (date < getTodayDate()) {
                return "The date of a drive should not be earlier than today's";
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/create_JSON")
                    .queryParam("userID", userID)
                    .queryParam("from", from)
                    .queryParam("to", to)
                    .queryParam("vacantPlaces", vacantPlaces)
                    .queryParam("date", date);

            ResultDrive resultDrive;

            while (true) {
                try {
                    resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                    break;
                } catch (Exception e) {
                    logger.error("Request: create drive, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultDrive);

            if (resultDrive.hasMessage()) {
                text = resultDrive.getMessage();
            } else {
                text = "You've created a drive with ID = " + resultDrive.getJsonDrive().getDriveID();
            }
        }
        return text;
    }

    private String getJoinDrive(Message message) {
        String[] strings = message.getText().split(" ");
        String text;
        List<String> parsedMessage = new ArrayList<>();

        for (String s : strings) {
            parsedMessage.add(s.trim());
        }

        if (parsedMessage.size() != 3) {
            text = "Wrong amount of command args";
        } else if (!parsedMessage.get(1).matches("[0-9]+")) {
            text = "Drive ID should contain only digits";
        } else if (!parsedMessage.get(2).matches("[0-9]+")) {
            text = "User ID should contain only digits";
        } else {
            long driveID = Long.parseLong(parsedMessage.get(1));
            long userID = Long.parseLong(parsedMessage.get(2));

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/join_drive_JSON")
                    .queryParam("userID", userID)
                    .queryParam("driveID", driveID);


            ResultDrive resultDrive;

            while (true) {
                try {
                    resultDrive = restTemplate.getForObject(builder.build().toString(), ResultDrive.class);
                    break;
                } catch (Exception e) {
                    logger.error("Request: join drive, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultDrive);

            if (resultDrive.hasMessage()) {
                text = resultDrive.getMessage();
            } else {
                text = "Successful!";
            }
        }
        return text;
    }

    private String getCreate(Message message) {
        String[] strings = message.getText().split(" ");
        String text;
        List<String> parsedMessage = new ArrayList<>();

        for (String s : strings) {
            parsedMessage.add(s.trim());
        }

        if (parsedMessage.size() != 3) {
            text = "Wrong amount of command args";
        } else {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/create_JSON")
                    .queryParam("login", parsedMessage.get(1))
                    .queryParam("password", parsedMessage.get(2));

            ResultUser resultUser;

            while (true) {
                try {
                    resultUser = restTemplate.getForObject(builder.build().toString(), ResultUser.class);
                    break;
                } catch (Exception e) {
                    logger.error("Request: create user, got an exception : '{}'", e.getMessage());
                }
            }

            Objects.requireNonNull(resultUser);

            if (resultUser.hasMessage()) {
                text = resultUser.getMessage();
            } else {
                text = "You've created a user with ID = " + resultUser.getJsonUser().getUserID();
            }
        }
        return text;
    }

    private String getSearch(Message message) {
        if (cities == null)
            getCitiesMap();
        String[] strings = message.getText().split(" ");
        String text;
        List<String> parsedMessage = new ArrayList<>();

        for (String s : strings) {
            parsedMessage.add(s.trim());
        }

        if (parsedMessage.size() != 9) {
            text = "Wrong amount of command args";
        } else if (!cities.containsKey(parsedMessage.get(1)) || !cities.containsKey(parsedMessage.get(2))) {
            text = "Wrong name of city. To check the names send \"/cities\"";
        } else {
            Long from = cities.get(parsedMessage.get(1));
            Long to = cities.get(parsedMessage.get(2));

            Calendar calendar = new GregorianCalendar(Integer.parseInt(parsedMessage.get(3)),
                    Integer.parseInt(parsedMessage.get(4)) - 1, Integer.parseInt(parsedMessage.get(5)));
            long dateFrom = calendar.getTime().getTime();

            calendar = new GregorianCalendar(Integer.parseInt(parsedMessage.get(6)),
                    Integer.parseInt(parsedMessage.get(7)) - 1, Integer.parseInt(parsedMessage.get(8)));
            long dateTo = calendar.getTime().getTime();

            if (dateFrom < getTodayDate() || dateTo < getTodayDate()) {
                return "Parameters of inquiry are invalid: dates should not be earlier than today's";
            }

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8282/hiber/searchDrive")
                    .queryParam("from", from)
                    .queryParam("to", to)
                    .queryParam("dateFrom", dateFrom)
                    .queryParam("dateTo", dateTo);

            ResponseEntity<Long[]> responseEntityDrives = restTemplate.getForEntity(builder.build().toString(), Long[].class);

            List<Long> drivesIDs = Arrays.asList(responseEntityDrives.getBody());

            if (drivesIDs.isEmpty())
                return "Nothing was found with this parameters";

            List<Drive> drives = new ArrayList<>();

            drivesIDs.forEach(driveID -> {

                UriComponentsBuilder builderForGet = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_JSON")
                        .queryParam("driveID", driveID);

                ResultDrive resultDrive;

                while (true) {
                    try {
                        resultDrive = restTemplate.getForObject(builderForGet.build().toString(), ResultDrive.class);
                        break;
                    } catch (Exception e) {
                        logger.error("Request: get drive, got an exception : '{}'", e.getMessage());
                    }
                }

                Objects.requireNonNull(resultDrive);

                if (!resultDrive.hasResult()) {
                    throw new RuntimeException("Something went really wrong: drive with ID " + driveID + " should be present in DB");
                }
                drives.add(resultDrive.getJsonDrive());
            });

            StringBuilder drivesText = new StringBuilder();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");
            for (Drive drive : drives) {
                drivesText.append("driveID: ").append(drive.getDriveID())
                        .append(" from: ").append(getCityName(drive.getFrom()).get())
                        .append(" to: ").append(getCityName(drive.getTo()).get())
                        .append(" vacant places: ").append(drive.getVacantPlaces())
                        .append(" filled places: ").append(drive.getJoinedUsers().size())
                        .append(" date: ").append(sdf.format(new Date(drive.getDate()))).append("\n");
            }
            text = drivesText.toString();

        }
        return text;
    }

    private String getAllDrives(Message message) {
        if (cities == null)
            getCitiesMap();
        StringBuilder drivesForString = new StringBuilder();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy MMM dd");

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/drives/get_all_JSON");

        ResponseEntity<Drive[]> responseEntity;

        while (true) {
            try {
                responseEntity = restTemplate.getForEntity(builder.build().toString(), Drive[].class);
                break;
            } catch (Exception e) {
                logger.error("Request: get all drives, got an exception : '{}'", e.getMessage());
            }
        }

        Objects.requireNonNull(responseEntity);
        List<Drive> drives = Arrays.asList(responseEntity.getBody());


        for (Drive drive : drives) {
            drivesForString.append("driveID: ").append(drive.getDriveID())
                    .append(" from: ").append(getCityName(drive.getFrom()).get())
                    .append(" to: ").append(getCityName(drive.getTo()).get())
                    .append(" vacant places: ").append(drive.getVacantPlaces())
                    .append(" filled places: ").append(drive.getJoinedUsers().size())
                    .append(" date: ").append(sdf.format(new Date(drive.getDate()))).append("\n");
        }
        return drivesForString.toString();
    }

    private String getAllUsers(Message message) {
        StringBuilder usersForString = new StringBuilder();

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl("http://localhost:8181/api/users/get_all_JSON");

        ResponseEntity<User[]> responseEntity;

        while (true) {
            try {
                responseEntity = restTemplate.getForEntity(builder.build().toString(), User[].class);
                break;
            } catch (Exception e) {
                logger.error("Request: get all users, got an exception : '{}'", e.getMessage());
            }
        }

        List<User> users = Arrays.asList(responseEntity.getBody());

        for (User user : users) {
            usersForString.append("userID: ").append(user.getUserID());

            if (!user.getJoinedDrives().isEmpty()) {
                usersForString.append(" joined drives: ");
                user.getJoinedDrives().forEach(v -> usersForString.append(v).append(" "));
            }
            if (!user.getPostedDrives().isEmpty()) {
                usersForString.append(" posted drives: ");
                user.getPostedDrives().forEach(v -> usersForString.append(v).append(" "));
            }

            usersForString.append("\n");
        }
        return usersForString.toString();
    }


    private String getHelp(Message message) {
        return "To create a user send \"/create login password\"."
                + "\nTo add a drive to your user send \"/add_drive yourID from_str to_str date:(yyyy mm dd) vacantPlaces\"."
                + "\nTo get names of the cities that exist now send \"/cities\""
                + "\nTo get all drives info send \"/get_all_drives\""
                + "\nTo get all users info send \"/get_all_users\""
                + "\nTo join to a drive send \"/join_drive driveID userID\""
                + "\nTo search drives send \"/search from_str to_str dateFrom:(yyyy mm dd) dateTo:(yyyy mm dd)\"";
    }

    private String getCities(Message message) {
        StringBuilder names = new StringBuilder();
        for (String s : cities.keySet()) {
            names.append(s).append("\n");
        }
        return names.toString();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(chatId);
            String text;

            if (message.getText().startsWith("/add_drive")) {
                text = getAddDrive(message);
                response.setText(text);
            } else if (message.getText().startsWith("/create")) {
                text = getCreate(message);
                response.setText(text);
            } else if (message.getText().startsWith("/help")) {
                text = getHelp(message);
                response.setText(text);
            } else if (message.getText().startsWith("/cities")) {
                text = getCities(message);
                response.setText(text);
            } else if (message.getText().startsWith("/get_all_drives")) {
                text = getAllDrives(message);
                response.setText(text);
            } else if (message.getText().startsWith("/get_all_users")) {
                text = getAllUsers(message);
                response.setText(text);
            } else if (message.getText().startsWith("/join_drive")) {
                text = getJoinDrive(message);
                response.setText(text);
            } else if (message.getText().startsWith("/search")) {
                text = getSearch(message);
                response.setText(text);
            } else {
                text = "Unknown command, please use \"/help\" to see command list";
                response.setText(text);
            }

            try {
                sendMessage(response);
                logger.info("Sent message \"{}\" to {}", text, chatId);
            } catch (TelegramApiException e) {
                logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
            }
        }
    }


    /**
     * Returns city name by its ID
     */
    private Optional<String> getCityName(Long ID) {
        if (cities == null)
            getCitiesMap();
        return Optional.ofNullable(cities.inverse().get(ID));
    }

    private Long getTodayDate() {
        Calendar date = new GregorianCalendar();

        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        return date.getTimeInMillis();
    }
}

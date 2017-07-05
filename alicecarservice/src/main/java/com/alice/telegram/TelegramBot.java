package com.alice.telegram;


import com.alice.Services.CitiesService;
import com.alice.Services.DriveService;
import com.alice.Services.UserService;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.User;
import liquibase.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


@Component
public class TelegramBot extends TelegramLongPollingBot
{

    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    @Value("${bot.token}")
    private String token;

    @Value("${bot.username}")
    private String username;

    @Autowired
    private UserService userService;

    @Autowired
    private DriveService driveService;

    @Autowired
    private CitiesService cities;

    @Override
    public String getBotToken()
    {
        return token;
    }


    @Override
    public String getBotUsername()
    {
        return username;
    }



    private String getAddDrive(Message message) {
        String[] strings = message.getText().split(" ");
        String text;
        List<String> parsedMessage = new ArrayList<>();

        for (String s : strings) {
            parsedMessage.add(s.trim());
        }

        if (parsedMessage.size() != 8) {
            text = "Wrong amount of command args";
        }
        else if (!parsedMessage.get(1).matches("[0-9]+")) {
            text = "User ID should contain only digits";
        }
        else if (userService.getUser(Long.parseLong(parsedMessage.get(1))) == null) {
            text = "User with this ID doesn't exist";
        }
        else if (!cities.getCityID(parsedMessage.get(2)).isPresent() || !cities.getCityID(parsedMessage.get(3)).isPresent()) {
            text = "Wrong name of city. To check the names send \"/cities\"";
        }
        else {
            long userID = Long.parseLong(parsedMessage.get(1));
            Long from = cities.getCityID(parsedMessage.get(2)).get();
            Long to = cities.getCityID(parsedMessage.get(3)).get();
            Calendar calendar = new GregorianCalendar(Integer.parseInt(parsedMessage.get(4)),
                    Integer.parseInt(parsedMessage.get(5)),Integer.parseInt(parsedMessage.get(6)));
            long date = calendar.getTime().getTime();
            int vacantPlaces = Integer.parseInt(parsedMessage.get(7));
            Drive drive = driveService.addDrive(userID, from, to, date, vacantPlaces);
            text = "You've tried to create a drive with ID = " + drive.getDriveID();
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
        }
        else if (!parsedMessage.get(1).matches("[0-9]+")) {
            text = "Drive ID should contain only digits";
        }
        else if (!parsedMessage.get(2).matches("[0-9]+")) {
            text = "User ID should contain only digits";
        }
        else if (driveService.getDrive(Long.parseLong(parsedMessage.get(1))) == null) {
            text = "Drive with this ID doesn't exist";
        }
        else if (userService.getUser(Long.parseLong(parsedMessage.get(2))) == null) {
            text = "User with this ID doesn't exist";
        }
        else {
            long driveID = Long.parseLong(parsedMessage.get(1));
            long userID = Long.parseLong(parsedMessage.get(2));
            Drive drive = driveService.joinDrive(driveID, userID);
            if (drive == null)
                text = "Couldn't join to a drive";
            else {
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
        }
        else {
            User user = userService.addUser(parsedMessage.get(1), parsedMessage.get(2));
            if (user == null) {
                text = "You've tried to create a user with already existing login";
            } else {
                text = "You've tried to create a user with ID = " + user.getUserID();
            }
        }
        return text;
    }

    private String getAllDrives(Message message) {
        StringBuilder drives = new StringBuilder();
        for (DriveView drive : driveService.getAllDrives()) {
            drives.append("driveID: ").append(drive.getDriveID())
                    .append(" from: ").append(cities.getCityName(drive.getFrom()).get())
                    .append(" to: ").append(cities.getCityName(drive.getTo()).get())
                    .append(" vacant places: ").append(drive.getVacantPlaces())
                    .append(" filled places: ").append(drive.getJoinedUsers().size()).append("\n");
        }
        return drives.toString();
    }

    private String getHelp(Message message) {
        return "To create a user send \"/create login password\".\nTo add a drive to your user send \"/add_drive yourID from_str to_str date:(yyyy mm dd) vacantPlaces\"."
                + "\nTo get names of the cities that exist now send \"/cities\"" + "\nTo get all drives info send \"/get_all_drives\""
                + "\nTo join to a drive send \"/join_drive driveID userID\"";
    }

    private String getCities(Message message) {
        StringBuilder names = new StringBuilder();
        for (String s : cities.getCitiesNames()) {
            names.append(s).append("\n");
        }
        return names.toString();
    }

    @Override
    public void onUpdateReceived(Update update)
    {
        if (update.hasMessage())
        {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(chatId);
            String text;

            if (message.getText().startsWith("/add_drive")) {
                text = getAddDrive(message);
                response.setText(text);
            }
            else if (message.getText().startsWith("/create")) {
                text = getCreate(message);
                response.setText(text);
            }
            else if (message.getText().startsWith("/help")) {
                text = getHelp(message);
                response.setText(text);
            }
            else if (message.getText().startsWith("/cities")) {
                text = getCities(message);
                response.setText(text);
            }
            else if (message.getText().startsWith("/get_all_drives")) {
                text = getAllDrives(message);
                response.setText(text);
            }
            else if (message.getText().startsWith("/join_drive")) {
                text = getJoinDrive(message);
                response.setText(text);
            }
            else {
                text = "Unknown command, please use \"/help\" to see command list";
                response.setText(text);
            }

            try {
                sendMessage(response);
                logger.info("Sent message \"{}\" to {}", text, chatId);
            }
            catch (TelegramApiException e)
            {
                logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
            }
        }
    }




}

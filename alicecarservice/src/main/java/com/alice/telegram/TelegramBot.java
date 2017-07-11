package com.alice.telegram;


import com.alice.dbclasses.user.UserView;
import com.alice.services.CitiesService;
import com.alice.services.DriveService;
import com.alice.services.UserService;
import com.alice.dbclasses.drive.Drive;
import com.alice.dbclasses.drive.DriveView;
import com.alice.dbclasses.user.User;
import com.alice.utils.Result;
import com.sun.org.apache.regexp.internal.RE;
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
            Result<DriveView> drive = driveService.addDrive(userID, from, to, date, vacantPlaces);
            if (!drive.isPresent()) {
                text = drive.getMessage();
            }
            else {
                text = "You've tried to create a drive with ID = " + drive.getResult().getDriveID();
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
        }
        else if (!parsedMessage.get(1).matches("[0-9]+")) {
            text = "Drive ID should contain only digits";
        }
        else if (!parsedMessage.get(2).matches("[0-9]+")) {
            text = "User ID should contain only digits";
        }
        else {
            long driveID = Long.parseLong(parsedMessage.get(1));
            long userID = Long.parseLong(parsedMessage.get(2));
            Result<DriveView> drive = driveService.joinDrive(driveID, userID);
            if (!drive.isPresent())
                text = drive.getMessage();
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
            Result<UserView> user = userService.addUser(parsedMessage.get(1), parsedMessage.get(2));
            if (!user.isPresent()) {
                text = user.getMessage();
            } else {
                text = "You've tried to create a user with ID = " + user.getResult().getUserID();
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

    private String getAllUsers(Message message) {
        StringBuilder users = new StringBuilder();
        for (UserView user : userService.getAllUsers()) {
            users.append("userID: ").append(user.getUserID());

            if (!user.getJoinedDrives().isEmpty()) {
                users.append(" joined drives: ");
                user.getJoinedDrives().forEach(v -> users.append(v).append(" "));
            }
            if (!user.getPostedDrives().isEmpty()) {
                users.append(" posted drives: ");
                user.getPostedDrives().forEach(v -> users.append(v).append(" "));
            }

            users.append("\n");
        }
        return users.toString();
    }

    private String getHelp(Message message) {
        return "To create a user send \"/create login password\".\nTo add a drive to your user send \"/add_drive yourID from_str to_str date:(yyyy mm dd) vacantPlaces\"."
                + "\nTo get names of the cities that exist now send \"/cities\"" + "\nTo get all drives info send \"/get_all_drives\"" +
                "\nTo get all users info send \"/get_all_users\""
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
            else if (message.getText().startsWith("/get_all_users")) {
                text = getAllUsers(message);
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

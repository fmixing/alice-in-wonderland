package com.alice.telegram;


import com.alice.dbclasses.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.commandbot.TelegramLongPollingCommandBot;
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
    private Cities cities;

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




    @Override
    public void onUpdateReceived(Update update)
    {
        if (update.hasMessage())
        {
            Message message = update.getMessage();
            if (message.getText().startsWith("/add_drive")) {
                String[] strings = message.getText().split(" ");
                List<String> parsedMessage = new ArrayList<>();
                for (String s : strings) {
                    parsedMessage.add(s.trim());
                }
                SendMessage response = new SendMessage();
                Long chatId = message.getChatId();
                response.setChatId(chatId);
                String text;

                if (parsedMessage.size() != 8) {
                    text = "Wrong command args";
                    response.setText(text);
                }
                else {
                    long userID = Long.parseLong(parsedMessage.get(1));
                    long from = cities.getCityID(parsedMessage.get(2));
                    long to = cities.getCityID(parsedMessage.get(3));
                    if (from == -1 || to == -1) {
                        text = "Wrong name of city. To check the names send \"/cities\"";
                    }
                    Calendar calendar = new GregorianCalendar(Integer.parseInt(parsedMessage.get(4)),
                            Integer.parseInt(parsedMessage.get(5)),Integer.parseInt(parsedMessage.get(6)));
                    long date = calendar.getTime().getTime();
                    int vacantPlaces = Integer.parseInt(parsedMessage.get(7));
                    Drive drive = driveService.addDrive(userID, from, to, date, vacantPlaces);
                    text = "You've tried to create a drive with ID = " + drive.getUserID();
                    response.setText(text);
                }

                try
                {
                    sendMessage(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                }
                catch (TelegramApiException e)
                {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }

            }
            else if (message.getText().startsWith("/create")) {
                String[] strings = message.getText().split(" ");
                List<String> parsedMessage = new ArrayList<>();
                for (String s : strings) {
                    parsedMessage.add(s.trim());
                }
                SendMessage response = new SendMessage();
                Long chatId = message.getChatId();
                response.setChatId(chatId);
                String text;

                if (parsedMessage.size() != 3) {
                    text = "Wrong command args";
                    response.setText(text);
                } else {
                    User user = userService.addUser(parsedMessage.get(1), parsedMessage.get(2));
                    if (user == null) {
                        text = "You've tried to create a user with already existing login";
                    }
                    else {
                        text = "You've tried to create a user with ID = " + user.getUserID();
                    }
                    response.setText(text);
                }

                try {
                    sendMessage(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                } catch (TelegramApiException e) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }
            }
            else if (message.getText().startsWith("/help")) {
                SendMessage response = new SendMessage();
                Long chatId = message.getChatId();
                response.setChatId(chatId);
                String text = "To create a user send \"/create login password\".\nTo add a drive to your user send \"/add_drive yourID from_str to_str date:(yyyy mm dd) vacantPlaces\"."
                        + "\nTo get names of the cities that exist now send \"/cities\"";
                response.setText(text);
                try {
                    sendMessage(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                } catch (TelegramApiException e) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }
            }
            else if (message.getText().startsWith("/cities")) {
                SendMessage response = new SendMessage();
                Long chatId = message.getChatId();
                response.setChatId(chatId);
                StringBuilder names = new StringBuilder();
                for (String s : cities.getCitiesNames()) {
                    names.append(s).append("\n");
                }
                String text = names.toString();
                response.setText(text);
                try {
                    sendMessage(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                } catch (TelegramApiException e) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }
            }
            else {
                SendMessage response = new SendMessage();
                Long chatId = message.getChatId();
                response.setChatId(chatId);
                String text = message.getText();
                response.setText(text);
                try {
                    sendMessage(response);
                    logger.info("Sent message \"{}\" to {}", text, chatId);
                } catch (TelegramApiException e) {
                    logger.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
                }
            }
        }
    }

}

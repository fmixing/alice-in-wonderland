package com.alice.rest;

import com.alice.dbclasses.user.UserView;
import com.alice.services.LogPassService;
import com.alice.services.UserService;
import com.alice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.Optional;

@Controller
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @Autowired
    private LogPassService logPassService;

    @RequestMapping("create_JSON")
    public String createJSON(Model model,
                             @RequestParam(value = "login", required = true) String login,
                             @RequestParam(value = "password", required = true) String password) {
        Result<UserView> user;

        String error;

        try {
            user = userService.addUser(login, password);
            if (user.isPresent()) {
                model.addAttribute("jsonUser", user.getResult());
            } else {
                error = user.getMessage();
                model.addAttribute("message", error);
            }
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

    @RequestMapping("get_user_id")
    public String getUserID(
            Model model,
            @RequestParam(value = "login", required = true) String login,
            @RequestParam(value = "password", required = true) String password) {

        Optional<Long> ID;
        String error;
        try {
            ID = logPassService.getUserID(login, password);
            if (ID.isPresent()) {
                model.addAttribute("jsonID", ID.get());
            } else {
                model.addAttribute("message", "User with this log pass doesn't exist");
            }

        } catch (Exception e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";

    }


    @RequestMapping("get_JSON")
    public String getJSON(Model model, @RequestParam(value = "id", required = true) Long id) {
        Result<UserView> user;

        String error;

        try {
            user = userService.getUser(id);
            if (user.isPresent()) {
                model.addAttribute("jsonUser", user.getResult());
            } else {
                model.addAttribute("message", user.getMessage());
            }
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

    @RequestMapping("get_all_JSON")
    public @ResponseBody
    Collection<UserView> getAllJSON(Model model) {

        return userService.getAllUsers();
    }

}
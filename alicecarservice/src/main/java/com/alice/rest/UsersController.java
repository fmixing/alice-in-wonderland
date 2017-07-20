package com.alice.rest;

import com.alice.dbclasses.user.UserView;
import com.alice.services.UserService;
import com.alice.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;

@Controller
@RequestMapping("/api/users")
public class UsersController {

    @Autowired
    private UserService userService;

    @RequestMapping("create_JSON")
    public String createJSON(Model model,
                             @RequestParam(value="login", required=true) String login,
                             @RequestParam(value="password", required=true) String password) {
        Result<UserView> user;

        String error;

        try {
            user = userService.addUser(login, password);
            if (user.isPresent()) {
                model.addAttribute("jsonUser", user.getResult());
            }
            else {
                error = user.getMessage();
                model.addAttribute("message", error);
            }
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

    @RequestMapping("get_JSON")
    public String getJSON(Model model, @RequestParam(value="id", required=true) Long id) {
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
    public @ResponseBody Collection<UserView> getAllJSON(Model model) {

        return userService.getAllUsers();
    }

}

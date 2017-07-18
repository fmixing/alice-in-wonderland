package com.alice.rest;

import com.alice.dbclasses.user.UserView;
import com.alice.services.UserService;
import com.alice.utils.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/api/users")
public class UsersController {

    private static final Logger logger = LoggerFactory.getLogger(UsersController.class);

    @Autowired
    private UserService userService;

    @RequestMapping("get_JSON")
    public String getJSON(Model model, @RequestParam(value="id", required=true) Long id) {
        Result<UserView> user;

        String error;

        try {
            user = userService.getUser(id);
            model.addAttribute("jsonUser", user.getResult());
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

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

    @RequestMapping("get_all_JSON")
    public @ResponseBody Collection<UserView> getAllJSON(Model model) {

        return userService.getAllUsers();
    }



//    @RequestMapping("get_all_JSON")
//    public String getAllJSON(Model model) {
//        Collection<UserView> users;
//
//        String error;
//
//        try {
//            users = userService.getAllUsers();
//            model.addAttribute(users);
//        } catch (DataAccessException e) {
//            error = "Some problems with database occurred, please try again later";
//            model.addAttribute("message", error);
//        }
//
//        return "jsonTemplate";
//    }

}

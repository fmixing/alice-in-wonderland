package com.alice.rest;

import com.alice.dbclasses.drive.DriveView;
import com.alice.services.DriveService;
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
@RequestMapping("/api/drives")
public class DrivesController {

    @Autowired
    private DriveService driveService;

    @RequestMapping("create_JSON")
    public String createJSON(Model model,
                             @RequestParam(value="userID", required=true) Long userID,
                             @RequestParam(value="from", required=true) Long from,
                             @RequestParam(value="to", required=true) Long to,
                             @RequestParam(value="date", required=true) Long date,
                             @RequestParam(value="vacantPlaces", required=true) int vacantPlaces) {
        Result<DriveView> drive;

        String error;


        try {
            drive = driveService.addDrive(userID, from, to, date, vacantPlaces);
            if (drive.isPresent()) {
                model.addAttribute("jsonDrive", drive.getResult());
            }
            else {
                error = drive.getMessage();
                model.addAttribute("message", error);
            }
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

    @RequestMapping("join_drive_JSON")
    public String joinJSON(Model model,
                             @RequestParam(value="userID", required=true) Long userID,
                             @RequestParam(value="driveID", required=true) Long driveID) {
        Result<DriveView> drive;

        String error;

        try {
            drive = driveService.joinDrive(driveID, userID);
            if (drive.isPresent()) {
                model.addAttribute("jsonDrive", drive.getResult());
            }
            else {
                error = drive.getMessage();
                model.addAttribute("message", error);
            }
        } catch (DataAccessException e) {
            error = "Some problems with database occurred, please try again later";
            model.addAttribute("message", error);
        }

        return "jsonTemplate";
    }

    @RequestMapping("get_JSON")
    public String getJSON(Model model, @RequestParam(value="driveID", required=true) Long id) {
        Result<DriveView> user;

        String error;

        try {
            user = driveService.getDrive(id);
            if (user.isPresent()) {
                model.addAttribute("jsonDrive", user.getResult());
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
    Collection<DriveView> getAllJSON(Model model) {

        return driveService.getAllDrives();
    }

}

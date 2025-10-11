package com.ddbs.choroid_user_service.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@Controller
public class FrontEndController {

    @GetMapping("/users/view/{queryUsername}")
    public String displayUserUI(@PathVariable String queryUsername, Model model) {
//        model.addAttribute("accessorUsername", accessorUsername);
        model.addAttribute("queryUsername", queryUsername);
        return "viewApp";
    }

    @GetMapping("/users/edit")
    public String updateUserUI(Model model) {
//        model.addAttribute("accessorUsername", accessorUsername);
        return "updateApp";
    }

    @GetMapping("/users/create")
    public String createUserUI(Model model) {
//        model.addAttribute("username", username);
        return "createApp";
    }

    @GetMapping("/users/search")
    public String searchUserUI(Model model) {
//        model.addAttribute("accessorUsername", accessorUsername);
        return "searchApp";
    }

}

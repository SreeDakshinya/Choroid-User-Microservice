package com.ddbs.choroid_user_service.ui;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class FrontEndController {

    @GetMapping("/{accessorId}/users/view/{queryId}")
    public String displayUserUI(@PathVariable long accessorId,
                                  @PathVariable long queryId,
                                  Model model) {
        model.addAttribute("accessorId", accessorId);
        model.addAttribute("queryId", queryId);
        return "viewApp";
    }

    @GetMapping("/{accessorId}/users/edit")
    public String updateUserUI(@PathVariable long accessorId,
                                Model model) {
        model.addAttribute("accessorId", accessorId);
        return "updateApp";
    }

    @GetMapping("/users/create")
    public String createUserUI(Model model) {
        return "createApp";
    }

    @GetMapping("/{accessorId}/users/search")
    public String searchUserUI(@PathVariable long accessorId,
                               Model model) {
        model.addAttribute("accessorId", accessorId);
        return "searchApp";
    }

}

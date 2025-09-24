package com.ddbs.choroid_user_service.rest;

import com.ddbs.choroid_user_service.model.SearchQueryUser;
import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.service.CreateService;
import com.ddbs.choroid_user_service.service.SearchService;
import com.ddbs.choroid_user_service.service.UpdateService;
import com.ddbs.choroid_user_service.service.ViewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class UserController {

    private final ViewService viewService;
    private final UpdateService updateService;
    private final CreateService createService;
    private final SearchService searchService;

    public UserController(ViewService viewService, UpdateService updateService, CreateService createService, SearchService searchService) {
        this.viewService = viewService;
        this.updateService = updateService;
        this.createService = createService;
        this.searchService = searchService;
    }

    @GetMapping("/{accessorId}/users/view/{queryId}")
    public ResponseEntity<User> displayUser(@PathVariable long accessorId, @PathVariable long queryId){
        User user = viewService.viewUserGivenId(accessorId, queryId);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{accessorId}/users/edit")
    public ResponseEntity<User> updateProfile(@PathVariable long accessorId, @RequestBody User newUser){
        try {
            User user = updateService.updateUserProfile(accessorId, newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/users/create")
    public ResponseEntity<User> createProfile(@RequestBody User newUser){
        try {
            User user = createService.createUserProfile(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{accessorId}/users/search")
    public ResponseEntity<List<User>> searchUser(@RequestBody SearchQueryUser queryUser){
        List<User> user = searchService.listMatchingUserProfiles(queryUser);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/users/topicstoteachlist")
    public ResponseEntity<List<String>> getTeachList(){
        List<String> topicsToTeach = searchService.getTopicsToTeach();
        return ResponseEntity.ok(topicsToTeach);
    }

    @GetMapping("/users/topicstolearnlist")
    public ResponseEntity<List<String>> getLearnList(){
        List<String> topicsToLearn = searchService.getTopicsToLearn();
        return ResponseEntity.ok(topicsToLearn);
    }

}

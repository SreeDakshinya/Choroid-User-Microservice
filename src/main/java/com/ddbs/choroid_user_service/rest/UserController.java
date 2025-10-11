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

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/users/api")
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

    @GetMapping("/{accessorUsername}/view/{queryUsername}")
    public ResponseEntity<User> displayUser(@PathVariable String accessorUsername, @PathVariable String queryUsername){
        User user = viewService.viewUserGivenId(accessorUsername, queryUsername);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{accessorUsername}/edit")
    public ResponseEntity<User> updateProfile(@PathVariable String accessorUsername, @RequestBody User newUser){
        try {
            User user = updateService.updateUserProfile(accessorUsername, newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/create")
    public ResponseEntity<User> createProfile(@RequestBody User newUser){
        try {
            User user = createService.createUserProfile(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(user);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{accessorUsername}/search")
    public ResponseEntity<List<User>> searchUser(@RequestBody SearchQueryUser queryUser){
        List<User> user = searchService.listMatchingUserProfiles(queryUser);
        return ResponseEntity.ok(user);
    }

    @GetMapping("/topicstoteachlist")
    public ResponseEntity<List<String>> getTeachList(){
        List<String> topicsToTeach = searchService.getTopicsToTeach();
        return ResponseEntity.ok(topicsToTeach);
    }

    @GetMapping("/topicstolearnlist")
    public ResponseEntity<List<String>> getLearnList(){
        List<String> topicsToLearn = searchService.getTopicsToLearn();
        return ResponseEntity.ok(topicsToLearn);
    }

    @GetMapping("/checkusername/{username}")
    public ResponseEntity<Boolean> checkUsernameHasProfile(@PathVariable String username){
        boolean exists = createService.checkUserExistsOrNot(username);
        return ResponseEntity.ok(exists);
    }

}

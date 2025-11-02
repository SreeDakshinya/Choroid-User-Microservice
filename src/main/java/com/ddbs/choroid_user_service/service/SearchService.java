package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.SearchQueryUser;
import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchService {

    public UserRepository userRepository;

    public SearchService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> listMatchingUserProfiles(SearchQueryUser queryUser) {
        return userRepository.listMatchingUsers(queryUser);
    }

    public List<String> getTopicsToTeach() {
        return userRepository.getTeachList();
    }

    public List<String> getTopicsToLearn() {
        return userRepository.getLearnList();
    }

    public List<String> listAllUserIds() {
        List<User> allUsers = userRepository.listMatchingUsers(new SearchQueryUser());
        List<String> userIdList = new ArrayList<>();
        for (User user: allUsers)
            userIdList.add(user.getUsername());
        return userIdList;
    }
}


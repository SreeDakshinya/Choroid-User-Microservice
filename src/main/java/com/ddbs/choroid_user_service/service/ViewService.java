package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class ViewService{

    public UserRepository userRepository;

    public ViewService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User viewUserGivenId(String accessorUsername, String queryUsername) {
        User userDescription = userRepository.findUserByUsername(queryUsername);

        if (!Objects.isNull(userDescription) && Objects.equals(accessorUsername, queryUsername)) {
            userDescription.setSelfAccess(true);
        }
        return userDescription;
    }

}

package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class CreateService{

    public UserRepository userRepository;

    public CreateService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUserProfile(User newUser) {
        return userRepository.createUser(newUser);
    }
}


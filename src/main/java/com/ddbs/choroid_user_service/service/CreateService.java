package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class CreateService{

    public UserRepository userRepository;

    public CreateService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean checkUserExistsOrNot(String username) {
        return !Objects.isNull(this.userRepository.findUserByUsername(username));
    }

    public User createUserProfile(User newUser) {
        return userRepository.createUser(newUser);
    }
}


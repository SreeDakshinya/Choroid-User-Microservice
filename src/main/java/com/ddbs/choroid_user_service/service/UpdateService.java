package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UpdateService{

    public UserRepository userRepository;

    public UpdateService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User updateUserProfile(long accessorId, User newUser) {
        return userRepository.updateUserById(accessorId, newUser);
    }

}

package com.ddbs.choroid_user_service.service;

import com.ddbs.choroid_user_service.model.User;
import com.ddbs.choroid_user_service.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class ViewService{

    public UserRepository userRepository;

    public ViewService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User viewUserGivenId(long accessorId, long queryId) {
        User userDescription = userRepository.findUserById(queryId);

        if (accessorId==queryId) {
            userDescription.setSelfAccess(true);
        }
        return userDescription;
    }

}

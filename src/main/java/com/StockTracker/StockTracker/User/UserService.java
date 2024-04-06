package com.StockTracker.StockTracker.User;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query; // Import the correct Query class
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public String findUserEmailByUsername(String username){
        Optional<User> userOptional = userRepository.findByUsername(username);
        if(userOptional.isPresent()){
            return userOptional.get().getEmail();
        } else {
            return null; // Or handle it based on your requirement
        }
    }

    public User findUserByUsername(String username){
        Optional<User> userOptional = userRepository.findByUsername(username);
        return userOptional.orElse(null); // Or handle the absence of the user differently

    }

    public boolean checkPassword(User user, String enteredPassword) {
        return user.getPassword().equals(enteredPassword);
    }

    public User createUser(User user) {
        User savedUser = userRepository.insert(user);
        return savedUser;
    }









}

package de.msg.javatraining.donationmanager.service;

import de.msg.javatraining.donationmanager.config.security.WebSecurityConfig;
import de.msg.javatraining.donationmanager.exception.*;
import de.msg.javatraining.donationmanager.persistence.model.DTOs.UserDTO;
import de.msg.javatraining.donationmanager.persistence.model.Role;
import de.msg.javatraining.donationmanager.persistence.model.User;
import de.msg.javatraining.donationmanager.persistence.repository.UserRepositoryInterface;
import de.msg.javatraining.donationmanager.persistence.repository.impl.RoleRepositoryInterfaceImpl;
import jdk.jshell.spi.ExecutionControl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import de.msg.javatraining.donationmanager.persistence.model.ERole;


import java.util.*;
import java.util.stream.Collectors;

import static de.msg.javatraining.donationmanager.persistence.model.DTOs.UserMapper.mapUserDTOToUser;

@Service
public class UserService {

    @Autowired
    private UserRepositoryInterface userRepository;

    @Autowired
    WebSecurityConfig webSecurityConfig;

    @Autowired
    private RoleRepositoryInterfaceImpl roleRepositoryInterface;

    @Autowired
    private JavaMailSender javaMailSender;



    public User createUser(UserDTO userDTO) throws IllegalArgumentException{
        validateUserInput(userDTO);

        //Converts userDto to user
        User user = mapUserDTOToUser(userDTO);

        //Username generation
        String generatedUsername = generateUniqueUsername(user.getFirstName(), user.getLastName());

        user.setUsername(generatedUsername);

        //Initial Password generation
        String initialPassword = generateInitialPassword();
        user.setPassword(initialPassword);

        System.out.println("ok");
        sendWelcomeEmail(user.getEmail(), initialPassword);
        System.out.println("nu ok");
        //initial login count 0 & is_active status true
        user.setLoginCount(0);
        user.setActive(true);

        List<Role> roles = processRoles(userDTO.getRoles());
        user.setRoles(roles);

        return userRepository.save(user);
    }

    private void sendWelcomeEmail(String email, String initialPassword) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Welcome to Donation Manager");
        mailMessage.setText("Welcome to our application, Now your personal information will be stolen and a clone will replace you in the society! Your initial password is: " + initialPassword);
        try {
            javaMailSender.send(mailMessage);
        }catch (MailException e){
            System.err.println("Error sending email:" + e);
        }
    }


    // inside methods
    private List<Role> processRoles(String[] roleNames) {
        List<Role> roles = new ArrayList<>();
        for (String roleName : roleNames) {
            try {
                ERole eRole = ERole.valueOf(roleName); // Convert roleName to ERole enum
                Role role = roleRepositoryInterface.findByName(eRole); // Find Role by ERole enum
                if (role != null) {
                    roles.add(role);
                }
            } catch (IllegalArgumentException e) {
                System.err.println("Something wrong with setting the roles");
            }
        }
        return roles;
    }


    private String generateInitialPassword() {
        UUID uuid = UUID.randomUUID();
        String initialPassword = uuid.toString().replace("-","").substring(0,10); // For 10 character long Password

        return initialPassword;
    }

    public String generateUniqueUsername(String firstName, String lastName) {
        String baseUsername = (lastName.substring(0, Math.min(5, lastName.length())) +
                firstName.substring(0, 1)).toLowerCase();

        int count = 1;
        String username = baseUsername;
        username = baseUsername + count;
        while (userRepository.existsByUsername(username)) {
            count++;
            username = baseUsername + count;
        }

        return username;
    }



    private boolean validateUserInput(UserDTO userDTO) {
        // Check if email is already existing
        boolean isEmailExisting = userRepository.existsByEmail(userDTO.getEmail());
        if (isEmailExisting) {
            throw new EmailAlreadyExistsException("Email already exists in the database");
        }

        // Check if mobile number is already existing
        boolean isMobileNumberExisting = userRepository.existsByMobileNumber(userDTO.getMobileNumber());
        if (isMobileNumberExisting) {
            throw new MobileNumberAlreadyExistsException("Mobile number already exists in the database");
        }

        // All checks passed
        return true;

    }
    //TODO: implement method
    private boolean checkPassword(Long userId, String password){
        return true;
    }

    public int changeUserPassword(User user, String newPassword) throws Exception {
        Long userId = user.getId();
        String userPassword = user.getPassword();

        boolean checkUserPassword = checkPassword(userId, userPassword);

        if(checkUserPassword) {
            userRepository.changeUserPassword(webSecurityConfig.passwordEncoder().encode(newPassword));
            return 1;
        }
        return 0;
    }

    public void updateLoginCount(Long userId, int newLoginCount) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setLoginCount(newLoginCount);
            userRepository.save(user);
        } else {
            System.out.println("FAILED TO UPDATE LOGINCOUNT!");
        }
    }
}

package org.example.web.service;

import org.example.web.model.User;
import org.example.web.repository.UserRepository;
import org.example.web.dto.RegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User authenticateUser(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // Use BCrypt to verify password
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    // Register a new user from a RegisterRequest DTO
    public User registerUser(RegisterRequest req) {
        // Basic uniqueness checks
        if (userRepository.findByUsername(req.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.findByEmail(req.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        LocalDate dob;
        try {
            dob = LocalDate.parse(req.getDateOfBirth());
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid dateOfBirth format. Expected yyyy-MM-dd");
        }

        User user = new User(
                req.getUsername(),
                passwordEncoder.encode(req.getPassword()),
                req.getFirstName(),
                req.getLastName(),
                req.getEmail(),
                req.getPhone(),
                dob,
                User.UserType.REGULAR_USER
        );

        return userRepository.save(user);
    }



    public User getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getOrCreateDefaultUser() {
        return userRepository.findByUsername("demo")
                .orElseGet(() -> {
                    User defaultUser = new User();
                    defaultUser.setUsername("demo");
                    defaultUser.setPassword(passwordEncoder.encode("demo")); // Hash the password
                    defaultUser.setEmail("demo@example.com");
                    defaultUser.setFirstName("Demo");
                    defaultUser.setLastName("User");
                    defaultUser.setUserType(User.UserType.REGULAR_USER);
                    return userRepository.save(defaultUser);
                });
    }

    public UserRepository getUserRepository() {
        return userRepository;
    }

    public void initializeDefaultUsers() {
        // Create admin user if it doesn't exist
        if (!userRepository.findByUsername("admin").isPresent()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin")); // Hash the password
            admin.setEmail("admin@example.com");
            admin.setFirstName("Admin");
            admin.setLastName("User");
            admin.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
            admin.setUserType(User.UserType.ADMIN);
            userRepository.save(admin);
        }

        // Create test user 1 if it doesn't exist
        if (!userRepository.findByUsername("user").isPresent()) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password")); // Hash the password
            user.setEmail("user@example.com");
            user.setFirstName("Alice");
            user.setLastName("Johnson");
            user.setDateOfBirth(java.time.LocalDate.of(1990, 1, 1));
            user.setUserType(User.UserType.REGULAR_USER);
            userRepository.save(user);
        }

        // Create test user 2 if it doesn't exist
        if (!userRepository.findByUsername("user2").isPresent()) {
            User user2 = new User();
            user2.setUsername("user2");
            user2.setPassword(passwordEncoder.encode("password2")); // Hash the password
            user2.setEmail("user2@example.com");
            user2.setFirstName("Bob");
            user2.setLastName("Wilson");
            user2.setDateOfBirth(java.time.LocalDate.of(1985, 5, 15));
            user2.setUserType(User.UserType.REGULAR_USER);
            userRepository.save(user2);
        }

        // Create test user 3 if it doesn't exist
        if (!userRepository.findByUsername("user3").isPresent()) {
            User user3 = new User();
            user3.setUsername("user3");
            user3.setPassword(passwordEncoder.encode("password3")); // Hash the password
            user3.setEmail("user3@example.com");
            user3.setFirstName("Carol");
            user3.setLastName("Davis");
            user3.setDateOfBirth(java.time.LocalDate.of(1992, 8, 22));
            user3.setUserType(User.UserType.REGULAR_USER);
            userRepository.save(user3);
        }
    }
}

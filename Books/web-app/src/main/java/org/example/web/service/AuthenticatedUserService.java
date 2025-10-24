package org.example.web.service;

import org.example.web.model.User;
import org.example.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class AuthenticatedUserService {

    @Autowired
    private UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new RuntimeException("User is not authenticated");
        }

        Object principal = auth.getPrincipal();
        if (principal instanceof User) {
            return (User) principal;
        }

        if (principal instanceof UserDetails) {
            String username = ((UserDetails) principal).getUsername();
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        }

        if (principal instanceof String) {
            String username = (String) principal;
            return userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Authenticated user not found in database"));
        }

        throw new RuntimeException("Unsupported principal type: " + principal.getClass().getName());
    }
}


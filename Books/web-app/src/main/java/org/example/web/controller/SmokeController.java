package org.example.web.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/smoke")
@CrossOrigin(origins = "*")
public class SmokeController {

    // simple login-like endpoint for smoke tests (optional)
    @PostMapping("/auth/login-smoke")
    public ResponseEntity<?> loginSmoke(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        if ("user1".equals(username) && "pass1".equals(password)) {
            return ResponseEntity.ok(Map.of("token", "dummy-jwt-token"));
        }
        return ResponseEntity.status(401).build();
    }

    @PostMapping("/reservations")
    public ResponseEntity<?> reservationSmoke(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                              @RequestBody Map<String, Object> body) {
        // no auth required for this smoke endpoint, return 201
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/comments")
    public ResponseEntity<?> commentSmoke(@RequestHeader(value = "Authorization", required = false) String authHeader,
                                          @RequestBody Map<String, Object> body) {
        // no auth required for this smoke endpoint, return 201
        return ResponseEntity.status(201).build();
    }
}


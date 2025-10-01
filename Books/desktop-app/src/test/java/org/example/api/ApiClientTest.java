package org.example.api;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.ExecutionException;
import org.example.model.User;

class ApiClientTest {
    @Test
    void testLoginWithInvalidCredentials() {
        ApiClient apiClient = new ApiClient();
        ExecutionException thrown = assertThrows(ExecutionException.class, () -> {
            apiClient.login("invalidUser", "wrongPassword").get();
        });
        assertNotNull(thrown.getMessage());
    }

    // Add more integration tests for API endpoints as needed
}


package org.example.web;

import org.example.web.model.User;
import org.example.web.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
class SecurityEndpointsWebTestClientTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @Test
    void createBook_requiresAdmin() throws Exception {
        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hobitas\"}"))
                .andExpect(status().isForbidden()); // 403 for unauthenticated or unauthorized
    }

    @Test
    void createBook_asAdmin_ok() throws Exception {
        // Arrange: create a User with a known ID and ROLE_ADMIN authority
        String userId = "test-user-id";
        User user = new User();
        user.setUsername("admin");
        user.setUserType(User.UserType.ADMIN);
        user.setId(userId);

        // Set up Authentication with ROLE_ADMIN
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null, authorities);

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        mockMvc.perform(post("/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"Hobitas\",\"author\":\"J.R.R. Tolkien\"}"))
                .andExpect(status().isCreated());
    }
}

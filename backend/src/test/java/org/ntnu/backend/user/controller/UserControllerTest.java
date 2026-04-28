package org.ntnu.backend.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.ntnu.backend.user.dto.CreateUserRequest;
import org.ntnu.backend.user.dto.LoginRequest;
import org.ntnu.backend.user.dto.LoginResponse;
import org.ntnu.backend.user.model.enums.RoleEnum;
import org.ntnu.backend.user.service.UserService;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);

        UserController userController = new UserController(userService);

        ReflectionTestUtils.setField(userController, "refreshExpiration", 604800000L);
        ReflectionTestUtils.setField(userController, "cookieSecure", false);

        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();

        objectMapper = new ObjectMapper();

        mockMvc = MockMvcBuilders
                .standaloneSetup(userController)
                .setValidator(validator)
                .build();
    }

    @Test
    void register_shouldReturnAccessTokenAndSetRefreshCookie() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("student@example.com");
        request.setPassword("Password123!");
        request.setRole(RoleEnum.STUDENT);

        LoginResponse response = new LoginResponse("access-token", "refresh-token");

        when(userService.register(any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("Secure"))));

        verify(userService).register(any(CreateUserRequest.class));
    }

    @Test
    void login_shouldReturnAccessTokenAndSetRefreshCookie() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("student@example.com");
        request.setPassword("Password123!");
        request.setPortalRole(RoleEnum.STUDENT);

        LoginResponse response = new LoginResponse("access-token", "refresh-token");

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("Secure"))));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void refresh_shouldReturnNewAccessTokenAndSetNewRefreshCookie() throws Exception {
        LoginResponse response = new LoginResponse("new-access-token", "new-refresh-token");

        when(userService.refresh("old-refresh-token")).thenReturn(response);

        mockMvc.perform(post("/auth/refresh")
                        .cookie(new Cookie("refreshToken", "old-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=new-refresh-token")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("Secure"))));

        verify(userService).refresh("old-refresh-token");
    }

    @Test
    void logout_withRefreshToken_shouldCallServiceAndClearCookie() throws Exception {
        mockMvc.perform(post("/auth/logout")
                        .cookie(new Cookie("refreshToken", "refresh-token")))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("Secure"))));

        verify(userService).logout("refresh-token");
    }

    @Test
    void logout_withoutRefreshToken_shouldClearCookieWithoutCallingService() throws Exception {
        mockMvc.perform(post("/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("refreshToken=")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Max-Age=0")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("HttpOnly")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("Path=/api/auth/refresh")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, containsString("SameSite=Lax")))
                .andExpect(header().string(HttpHeaders.SET_COOKIE, not(containsString("Secure"))));

        verify(userService, never()).logout(any());
    }

    @Test
    void register_withInvalidRequest_shouldReturnBadRequestAndNotCallService() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setEmail("not-an-email");
        request.setPassword("short");
        request.setRole(null);

        mockMvc.perform(post("/auth/register")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).register(any(CreateUserRequest.class));
    }

    @Test
    void login_withInvalidRequest_shouldReturnBadRequestAndNotCallService() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("not-an-email");
        request.setPassword("short");
        request.setPortalRole(null);

        mockMvc.perform(post("/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).login(any(LoginRequest.class));
    }
}
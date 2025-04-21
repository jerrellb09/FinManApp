package com.jay.home.finmanapp.controller;

import com.jay.home.finmanapp.model.User;
import com.jay.home.finmanapp.security.JwtUtils;
import com.jay.home.finmanapp.service.DemoDataService;
import com.jay.home.finmanapp.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private DemoDataService demoDataService;

    @InjectMocks
    private AuthController authController;

    @Test
    public void testDemoLogin_Success() {
        // Arrange
        User demoUser = new User();
        demoUser.setId(1L);
        demoUser.setEmail("demo@finmanapp.com");
        demoUser.setFirstName("Demo");
        demoUser.setLastName("User");
        demoUser.setIsDemo(true);
        demoUser.setMonthlyIncome(new BigDecimal("5000.00"));
        demoUser.setPaydayDay(15);

        when(userService.getDemoUser()).thenReturn(demoUser);
        when(jwtUtils.generateToken(anyString())).thenReturn("mock-jwt-token");

        // Act
        ResponseEntity<?> response = authController.demoLogin(
                "https://justjay.net/some/page",
                "true",
                "true",
                "justjay.net");

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userService, times(1)).getDemoUser();
        verify(demoDataService, times(1)).initializeDemoUserData();
        verify(jwtUtils, times(1)).generateToken(anyString());
    }

    @Test
    public void testDemoLogin_Fail_NotAuthorized() {
        // Arrange - no referer or query params
        
        // Act
        ResponseEntity<?> response = authController.demoLogin(
                null, // referer
                null, // X-Demo-Request header
                null, // demo param
                null  // source param
        );

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(userService, never()).getDemoUser();
        verify(demoDataService, never()).initializeDemoUserData();
    }
}
package com.example.journalApp.services;

import com.example.journalApp.entity.User;
import com.example.journalApp.repository.UserRepository;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@Disabled("Class is under refactoring")
@ExtendWith(MockitoExtension.class) //Added @ExtendWith(MockitoExtension.class) - This initializes the @Mock and @InjectMocks annotations
public class UserDetailServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailServiceImpl userDetailService;

    @Test
    void loadUserByUsernameTest(){
        User user = new User("ram", "12s651fsad");
        user.setRoles(new ArrayList<>());
        when(userRepository.findByUserName(ArgumentMatchers.anyString())).thenReturn(user);
        UserDetails userDetail = userDetailService.loadUserByUsername("ram");
        assertNotNull(userDetail);
    }
}

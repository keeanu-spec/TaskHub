package org.example.service;

import org.example.repository.memory.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.example.domain.User;
import org.example.domain.Role;
import static org.assertj.core.api.Assertions.*;

import java.util.UUID;

import org.example.exception.*;;

public class UserServiceTest {
    private UserRepository userRepository;
    private UserService userService;
    @BeforeEach
    void setup(){
        userRepository =  new UserRepository();
        userService = new UserService(userRepository);
    }

    @Test
    void create(){
     User user =   userService.create("Keeanu", "kea@gmail.com", "pass", Role.MEMBER);

        assertThat(user.getUsername()).isEqualTo("Keeanu");          
    }

    @Test
    void duplicated() {
        User user =  userService.create("Keeanu", "kea@gmail.com", "pass", Role.MEMBER);
       

        assertThatThrownBy(() -> userService.create("Keeanu1", "kea@gmail.com", "pass", Role.MEMBER))
        .isInstanceOf(DuplicateEntityException.class);
    }

    @Test
    void UserBlank(){
         

         assertThatThrownBy(() -> userService.create(" ", "kea@gmail.com", "pass", Role.MEMBER))
         .isInstanceOf(ValidationException.class);
    }
    
    @Test
    void UserBlankID(){
         
         UUID id = UUID.randomUUID();
         assertThatThrownBy(() -> userService.findById(id))
         .isInstanceOf(EntityNotFoundException.class);
    }
}

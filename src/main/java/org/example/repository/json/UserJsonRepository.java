package org.example.repository.json;

import java.nio.file.Path;
import java.util.UUID;

import org.example.domain.User;

public class UserJsonRepository extends JsonRepository<User,UUID>{
    
        public UserJsonRepository(){
            super(User::getId,Path.of("data/users.json"),User.class);
        }

}

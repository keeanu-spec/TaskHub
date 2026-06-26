package org.example.cli;

import java.util.HashMap;
import java.util.Optional;
import java.util.Collection;
public class CommandRegistry {
    private final HashMap<String, Command> commands = new HashMap<>();

        
    public void register(Command command){
         String name = command.getName();
         commands.put(name, command);
    }

    public Optional<Command> find(String command) {
      
        return Optional.ofNullable(commands.get(command));
    }
    public Collection<Command> getAll(){
        return commands.values();
    }
}

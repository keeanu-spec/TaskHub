package org.example.cli.commands;

import org.example.cli.Command;
import org.example.cli.CommandContext;

public class ExitCommand implements Command{
    

    public String getName(){
        return "exit";
    }

    public String getDescription(){
        return "Just Terminal close ";
    }

    public void execute(CommandContext context) {
        System.exit(0);
    }


   
}

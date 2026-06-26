package org.example.cli.commands;

import org.example.cli.Command;
import org.example.cli.CommandContext;

public class DiscordCommand implements Command{

    public String getName() {
        return "-D";
    }

    public String getDescription() {
        return "Discord";
    }


    public void execute(CommandContext context) {
         try {
            //Open discord URL its not really workiing need check V for see what happend-
            new ProcessBuilder("C:\\Users\\Acer\\AppData\\Local\\Discord\\app-1.0.9242\\Discord.exe").start();
        }catch(Exception e) {
            context.output().error(e.getMessage());
        }
    }
    
}

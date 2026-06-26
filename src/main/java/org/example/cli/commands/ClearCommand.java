package org.example.cli.commands;

import org.example.cli.Command;
import org.example.cli.CommandContext;

public class ClearCommand implements Command {

    public String getName() {
        return "--clear";
    }

    public String getDescription(){
        return "Clear all Characters on cmd";
    }

    public void execute(CommandContext context){
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }
}

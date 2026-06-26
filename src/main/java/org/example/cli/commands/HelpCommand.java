    package org.example.cli.commands;

   

import org.example.cli.Command;
import org.example.cli.CommandContext;


    public class HelpCommand implements Command{

        
        public String getName(){
            return "Help";
        }
        
        public String getDescription(){
            return "A basic description not too long";
        }

       
        
        public void execute(CommandContext context) {
          
            for(Command cmd : context.commandRegistry().getAll()) {
                context.output().info(cmd.getName() + " - " + cmd.getDescription());
            }
            
        }
    }

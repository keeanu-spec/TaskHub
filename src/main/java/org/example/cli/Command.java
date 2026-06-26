package org.example.cli;


    //This class nedeed for All commands  its give the ifnromaction with String metods to Help! = commands
    //Execute give the chances
    public interface Command {
        String getName();
        String getDescription();
        void execute(CommandContext context);
    }

   



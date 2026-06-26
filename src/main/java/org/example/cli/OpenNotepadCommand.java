package org.example.cli;


public class OpenNotepadCommand  implements Command {

    public String getName(){
        return "start-notepad";
    }
    public String getDescription(){
        return "Open Notepad";
    }

    public void execute(CommandContext context){
        try{
        Runtime.getRuntime().exec("notepad.exe");
        }catch(Exception e) {
            context.output().error(e.getMessage());
        }

    }
}

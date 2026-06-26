package org.example.cli.commands;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.DashboardRenderer;

public class ClearCommand implements Command {

    @Override public String getName()        { return "clear"; }
    @Override public String getDescription() { return "Limpia la pantalla y muestra el dashboard"; }

    @Override
    public void execute(CommandContext context) {
        System.out.print("\033[H\033[2J");
        System.out.flush();

        String C = "\u001B[36m";
        String R = "\u001B[0m";
        System.out.println(C + "  / \\__         _  _____________  _   ___  __  ________    __ " + R);
        System.out.println(C + " (    @\\___     | |/ / ____/ ____|| | / / |/ / / ____/ /   /  |" + R);
        System.out.println(C + " /         O    | \'  /| |__ | |__  | |/ /|   / | /   | |   | | |" + R);
        System.out.println(C + "/   (_____/     |  < |  __||  __| |    \\|    \\ | |   | |   | | |" + R);
        System.out.println(C + "/_____/   U     |_|\\_\\|____||____||_|\\_\\|_|\\_\\ \\____/|_____|___/" + R);
        System.out.println();

        new DashboardRenderer(context.taskService(), context.projectService(), context.output()).render();
    }
}

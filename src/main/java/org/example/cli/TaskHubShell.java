package org.example.cli;

import org.jline.reader.LineReader;
import java.util.Optional;

public class TaskHubShell {

    private final LineReader lineReader;
    private final CommandRegistry commandRegistry;
    private final CommandContext commandContext;
    private final DashboardRenderer dashboard;

    public TaskHubShell(LineReader lineReader, CommandContext commandContext,
                        CommandRegistry commandRegistry, DashboardRenderer dashboard) {
        this.lineReader      = lineReader;
        this.commandRegistry = commandRegistry;
        this.commandContext  = commandContext;
        this.dashboard       = dashboard;
    }

    public void run() {
        printBanner();
        dashboard.render();

        while (true) {
            // Prompt dinámico según directorio actual
            String prompt = commandContext.shell().prompt();
            String line = lineReader.readLine(prompt);
            if (line == null || line.isBlank()) continue;

            Optional<Command> command = commandRegistry.find(line.trim());
            if (command.isEmpty()) {
                commandContext.output().error(
                    "Comando no encontrado: '" + line.trim().split("\\s+")[0] + "'  →  escribe 'help' para ver los disponibles.");
            } else {
                try {
                    String[] args = CommandRegistry.parseArgs(line);
                    command.get().execute(commandContext, args);
                } catch (IllegalArgumentException e) {
                    commandContext.output().error("Valor inválido: " + e.getMessage());
                } catch (Exception e) {
                    commandContext.output().error(e.getMessage() != null ? e.getMessage() : "Error inesperado.");
                }
            }
        }
    }

    private void printBanner() {
        String C = "\u001B[36m";
        String R = "\u001B[0m";
        System.out.println(C + "  / \\__         _  _____________  _   ___  __  ________    __ " + R);
        System.out.println(C + " (    @\\___     | |/ / ____/ ____|| | / / |/ / / ____/ /   /  |" + R);
        System.out.println(C + " /         O    | ' /| |__ | |__  | |/ /|   / | /   | |   | | |" + R);
        System.out.println(C + "/   (_____/     |  < |  __||  __| |    \\|    \\ | |   | |   | | |" + R);
        System.out.println(C + "/_____/   U     |_|\\_\\|____||____||_|\\_\\|_|\\_\\ \\____/|_____|___/" + R);
        System.out.println();
    }
}

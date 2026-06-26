package org.example.cli;

import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

public class CommandRegistry {

    private final HashMap<String, Command> commands = new HashMap<>();

    public void register(Command command) {
        commands.put(command.getName().toLowerCase(), command);
    }

    /**
     * Busca un comando por la primera palabra de la línea (case-insensitive).
     * Así "cd MiProyecto" encuentra el comando "cd".
     */
    public Optional<Command> find(String line) {
        if (line == null || line.isBlank()) return Optional.empty();
        String first = line.trim().split("\\s+")[0].toLowerCase();
        return Optional.ofNullable(commands.get(first));
    }

    /** Extrae los argumentos (todo lo que va después del nombre del comando). */
    public static String[] parseArgs(String line) {
        String trimmed = line.trim();
        int space = trimmed.indexOf(' ');
        if (space == -1) return new String[0];
        String rest = trimmed.substring(space + 1).trim();
        return rest.isEmpty() ? new String[0] : rest.split("\\s+", 2);
    }

    public Collection<Command> getAll() {
        return commands.values();
    }
}

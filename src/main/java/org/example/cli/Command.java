package org.example.cli;

/**
 * Interfaz base para todos los comandos de TaskHub.
 * Los comandos filesystem sobreescriben execute(ctx, args) para recibir argumentos inline.
 */
public interface Command {
    String getName();
    String getDescription();

    /** Ejecuta el comando sin argumentos (modo interactivo). */
    void execute(CommandContext context);

    /**
     * Ejecuta el comando con argumentos inline (ej: cd MiProyecto).
     * Por defecto ignora args y llama a execute(context).
     * Los comandos filesystem sobreescriben este método.
     */
    default void execute(CommandContext context, String[] args) {
        execute(context);
    }
}

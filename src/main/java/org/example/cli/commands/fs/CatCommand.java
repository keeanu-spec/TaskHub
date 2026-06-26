package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Task;
import org.example.domain.TaskNote;

import java.util.List;
import java.util.Optional;

public class CatCommand implements Command {

    private static final String R  = "\u001B[0m";
    private static final String B  = "\u001B[1m";
    private static final String D  = "\u001B[2m";
    private static final String CY = "\u001B[36m";

    @Override public String getName()        { return "cat"; }
    @Override public String getDescription() { return "Muestra detalles de una tarea (cat <título>)"; }

    @Override public void execute(CommandContext ctx) {
        ctx.output().warning("Uso: cat <título>  (dentro de un proyecto)");
    }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (!ctx.shell().isInProject()) {
            ctx.output().error("Entra en un proyecto primero: cd <nombre>");
            return;
        }
        if (args.length == 0) { ctx.output().warning("Uso: cat <título>"); return; }

        String name = String.join(" ", args).trim();
        Optional<Task> found = ctx.taskService()
            .findByProjectId(ctx.shell().currentProject().get().getId()).stream()
            .filter(t -> t.getTitle().equalsIgnoreCase(name))
            .findFirst();

        if (found.isEmpty()) { ctx.output().error("Tarea no encontrada: " + name); return; }

        Task t = found.get();
        System.out.println();
        System.out.println(B + CY + "  " + t.getTitle() + R);
        System.out.println(D + "  " + "─".repeat(50) + R);
        System.out.println("  Estado:       " + t.getStatus());
        System.out.println("  Prioridad:    " + t.getPriority());
        System.out.println("  Vencimiento:  " + (t.getDueDate() != null ? t.getDueDate() : "—"));
        System.out.println("  ID:           " + D + t.getId() + R);
        if (t.getDescription() != null && !t.getDescription().isBlank())
            System.out.println("\n  " + t.getDescription());

        List<TaskNote> notes = ctx.noteService().findByTaskId(t.getId());
        if (!notes.isEmpty()) {
            System.out.println("\n" + B + "  Notas:" + R);
            notes.forEach(n -> System.out.println(D + "  [" + n.getCreatedAt().toLocalDate() + "]" + R
                + "  " + n.getContent()));
        }
        System.out.println();
    }
}

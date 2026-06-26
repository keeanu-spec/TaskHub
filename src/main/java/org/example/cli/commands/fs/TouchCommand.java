package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Priority;
import org.example.domain.Status;
import org.example.domain.Task;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TouchCommand implements Command {

    @Override public String getName()        { return "touch"; }
    @Override public String getDescription() { return "Crea una tarea en el proyecto actual (touch <título>)"; }

    @Override public void execute(CommandContext ctx) {
        if (!ctx.shell().isInProject())
            ctx.output().error("Entra en un proyecto primero: cd <nombre>");
        else
            execute(ctx, new String[0]);
    }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (!ctx.shell().isInProject()) {
            ctx.output().error("Entra en un proyecto primero: cd <nombre>");
            return;
        }

        String title = args.length > 0 ? String.join(" ", args).trim() : "";
        if (title.isBlank()) title = ctx.prompter().prompt("Título de la tarea: ").trim();

        String priStr = ctx.prompter().prompt("Prioridad [LOW/MEDIUM/HIGH/CRITICAL] (enter=MEDIUM): ").trim();
        Priority priority = priStr.isBlank() ? Priority.MEDIUM : Priority.valueOf(priStr.toUpperCase());

        String dueStr = ctx.prompter().prompt("Fecha vencimiento [YYYY-MM-DD] (enter=ninguna): ").trim();
        LocalDate dueDate = null;
        if (!dueStr.isBlank()) {
            try { dueDate = LocalDate.parse(dueStr); }
            catch (DateTimeParseException e) { ctx.output().warning("Fecha inválida, se ignora."); }
        }

        Task task = ctx.taskService().create(
            title, null, null, Status.TODO, priority,
            ctx.shell().currentProject().get(), dueDate);
        ctx.output().success("Tarea creada: \"" + task.getTitle() + "\"");
    }
}

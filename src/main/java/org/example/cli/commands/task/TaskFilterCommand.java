package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Priority;
import org.example.domain.Status;
import org.example.domain.Task;

import java.util.List;

/**
 * Filtra tareas por proyecto, prioridad, estado o vencimiento.
 *
 * Uso en el prompt:
 *   Task-Filter
 *   → project: web   priority: HIGH   status: TODO   due-today: s/n
 *
 * Dejar en blanco = sin filtro para ese campo.
 */
public class TaskFilterCommand implements Command {

    @Override
    public String getName() { return "Task-Filter"; }

    @Override
    public String getDescription() { return "Filtra tareas por proyecto, prioridad, estado o fecha"; }

    @Override
    public void execute(CommandContext context) {
        String projectName = context.prompter().prompt("Proyecto (enter = todos): ").trim();
        String priorityStr = context.prompter().prompt("Prioridad [LOW/MEDIUM/HIGH/CRITICAL] (enter = todas): ").trim();
        String statusStr   = context.prompter().prompt("Estado [TODO/IN_PROGRESS/DONE/CANCELLED] (enter = todos): ").trim();
        String dueTodayStr = context.prompter().prompt("Solo vencen hoy? [s/n] (enter = no): ").trim();

        Priority priority  = priorityStr.isBlank()  ? null : Priority.valueOf(priorityStr.toUpperCase());
        Status   status    = statusStr.isBlank()    ? null : Status.valueOf(statusStr.toUpperCase());
        boolean  dueToday  = dueTodayStr.equalsIgnoreCase("s");

        String project = projectName.isBlank() ? null : projectName;

        List<Task> tasks = context.taskService().findByFilters(project, priority, status, dueToday);

        if (tasks.isEmpty()) {
            context.output().warning("No se encontraron tareas con esos filtros.");
            return;
        }

        context.output().info(String.format("%-36s  %-30s  %-12s  %-10s  %s",
                "ID", "Título", "Prioridad", "Estado", "Vence"));
        context.output().info("─".repeat(110));

        for (Task t : tasks) {
            String due = t.getDueDate() != null ? t.getDueDate().toString() : "—";
            context.output().print(String.format("%-36s  %-30s  %-12s  %-10s  %s",
                    t.getId(),
                    truncate(t.getTitle(), 30),
                    t.getPriority(),
                    t.getStatus(),
                    due));
        }
        context.output().info("Total: " + tasks.size() + " tarea(s)");
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

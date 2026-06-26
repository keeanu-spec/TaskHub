package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.TimeEntry;
import org.example.service.TimeTrackingService;

import java.util.List;
import java.util.UUID;

public class TaskTimeCommand implements Command {

    @Override public String getName()        { return "Task-Time"; }
    @Override public String getDescription() { return "Muestra el tiempo total invertido en una tarea"; }

    @Override
    public void execute(CommandContext context) {
        String taskIdStr = context.prompter().prompt("ID de la tarea: ").trim();
        UUID taskId = UUID.fromString(taskIdStr);
        context.taskService().findById(taskId); // valida que existe

        List<TimeEntry> entries = context.timeService().findByTaskId(taskId);
        long total = context.timeService().totalMinutes(taskId);

        if (entries.isEmpty()) {
            context.output().warning("No hay tiempo registrado para esta tarea.");
            return;
        }

        context.output().info("Sesiones registradas (" + entries.size() + "):");
        for (TimeEntry e : entries) {
            String dur = TimeTrackingService.formatMinutes(e.getDurationMinutes());
            context.output().print("  " + e.getStartedAt().toLocalDate() + "  " + dur);
        }
        context.output().success("Total: " + TimeTrackingService.formatMinutes(total));
    }
}

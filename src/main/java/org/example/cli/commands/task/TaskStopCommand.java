package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.TimeEntry;
import org.example.service.TimeTrackingService;

import java.util.Optional;
import java.util.UUID;

public class TaskStopCommand implements Command {

    @Override public String getName()        { return "Task-Stop"; }
    @Override public String getDescription() { return "Para el timer activo"; }

    @Override
    public void execute(CommandContext context) {
        // Primero comprobar si hay algún timer activo
        Optional<TimeEntry> active = context.timeService().findActive();
        if (active.isEmpty()) {
            context.output().warning("No hay ningún timer activo.");
            return;
        }

        TimeEntry entry = context.timeService().stop(active.get().getTask().getId());
        String duration = TimeTrackingService.formatMinutes(entry.getDurationMinutes());
        context.output().success("⏹  Timer parado: \"" + entry.getTask().getTitle() + "\"  →  " + duration);
    }
}

package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Task;
import org.example.domain.TimeEntry;

import java.util.UUID;

public class TaskStartCommand implements Command {

    @Override public String getName()        { return "Task-Start"; }
    @Override public String getDescription() { return "Inicia el timer de una tarea"; }

    @Override
    public void execute(CommandContext context) {
        String taskIdStr = context.prompter().prompt("ID de la tarea: ").trim();
        UUID taskId = UUID.fromString(taskIdStr);
        Task task   = context.taskService().findById(taskId);

        TimeEntry entry = context.timeService().start(task);
        context.output().success("⏱  Timer iniciado: \"" + task.getTitle() + "\"  ["
            + entry.getStartedAt().toLocalTime().withNano(0) + "]");
    }
}

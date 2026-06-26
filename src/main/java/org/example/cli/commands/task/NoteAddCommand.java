package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Task;

import java.util.List;
import java.util.UUID;

public class NoteAddCommand implements Command {

    @Override public String getName()        { return "Note-Add"; }
    @Override public String getDescription() { return "Añade una nota a una tarea"; }

    @Override
    public void execute(CommandContext context) {
        String taskIdStr = context.prompter().prompt("ID de la tarea: ").trim();
        String content   = context.prompter().prompt("Nota: ").trim();

        UUID taskId = UUID.fromString(taskIdStr);
        Task task   = context.taskService().findById(taskId);
        context.noteService().addNote(task, content);
        context.output().success("Nota añadida.");
    }
}

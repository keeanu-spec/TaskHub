package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.TaskNote;

import java.util.List;
import java.util.UUID;

public class NoteListCommand implements Command {

    @Override public String getName()        { return "Note-List"; }
    @Override public String getDescription() { return "Lista las notas de una tarea"; }

    @Override
    public void execute(CommandContext context) {
        String taskIdStr = context.prompter().prompt("ID de la tarea: ").trim();
        UUID taskId = UUID.fromString(taskIdStr);

        // Verificar que la tarea existe
        context.taskService().findById(taskId);

        List<TaskNote> notes = context.noteService().findByTaskId(taskId);
        if (notes.isEmpty()) {
            context.output().warning("Esta tarea no tiene notas.");
            return;
        }

        context.output().info("Notas (" + notes.size() + "):");
        for (TaskNote note : notes) {
            context.output().print("  [" + note.getCreatedAt().toLocalDate() + "] " + note.getContent());
        }
    }
}

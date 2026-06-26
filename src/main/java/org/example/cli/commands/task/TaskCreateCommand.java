package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class TaskCreateCommand implements Command {

    @Override public String getName()        { return "Task-Create"; }
    @Override public String getDescription() { return "Crea una nueva tarea en un proyecto"; }

    @Override
    public void execute(CommandContext context) {
        // Proyecto
        String projectName = context.prompter().prompt("Nombre del proyecto: ").trim();
        Project project = context.projectService().findAll().stream()
            .filter(p -> p.getName().equalsIgnoreCase(projectName))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Proyecto no encontrado: " + projectName));

        // Datos de la tarea
        String title = context.prompter().prompt("Título: ").trim();
        String desc  = context.prompter().prompt("Descripción (opcional): ").trim();

        // Prioridad
        String priStr = context.prompter().prompt("Prioridad [LOW/MEDIUM/HIGH/CRITICAL] (enter = MEDIUM): ").trim();
        Priority priority = priStr.isBlank() ? Priority.MEDIUM : Priority.valueOf(priStr.toUpperCase());

        // Fecha de vencimiento
        String dueStr = context.prompter().prompt("Fecha de vencimiento [YYYY-MM-DD] (enter = sin fecha): ").trim();
        LocalDate dueDate = null;
        if (!dueStr.isBlank()) {
            try { dueDate = LocalDate.parse(dueStr); }
            catch (DateTimeParseException e) {
                context.output().warning("Formato de fecha inválido, se ignora.");
            }
        }

        Task task = context.taskService().create(
            title,
            null,
            desc.isBlank() ? null : desc,
            Status.TODO,
            priority,
            project,
            dueDate
        );

        context.output().success("Tarea creada: \"" + task.getTitle() + "\" [" + task.getId() + "]");
    }
}

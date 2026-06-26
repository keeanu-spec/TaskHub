package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.export.MarkdownExporter;
import org.example.domain.Project;
import org.example.domain.Task;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Exporta las tareas de un proyecto a un archivo Markdown en el directorio actual.
 *
 * Uso: Export-Project → te pregunta el nombre del proyecto
 */
public class ExportProjectCommand implements Command {

    @Override
    public String getName() { return "Export-Project"; }

    @Override
    public String getDescription() { return "Exporta las tareas de un proyecto a Markdown (.md)"; }

    @Override
    public void execute(CommandContext context) {
        String projectName = context.prompter().prompt("Nombre del proyecto a exportar: ").trim();
        if (projectName.isBlank()) {
            context.output().error("Debes indicar un nombre.");
            return;
        }

        Optional<Project> projectOpt = context.projectService().findAll().stream()
                .filter(p -> p.getName().equalsIgnoreCase(projectName))
                .findFirst();

        if (projectOpt.isEmpty()) {
            context.output().error("Proyecto no encontrado: " + projectName);
            return;
        }

        Project project = projectOpt.get();
        List<Task> tasks = context.taskService().findByProjectId(project.getId());

        if (tasks.isEmpty()) {
            context.output().warning("El proyecto no tiene tareas.");
            return;
        }

        try {
            Path file = new MarkdownExporter().export(project, tasks);
            context.output().success("Exportado: " + file.toAbsolutePath());
            context.output().info("Total: " + tasks.size() + " tarea(s)");
        } catch (Exception e) {
            context.output().error("Error al exportar: " + e.getMessage());
        }
    }
}

package org.example.cli.export;

import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exporta las tareas de un proyecto a un archivo Markdown.
 */
public class MarkdownExporter {

    /**
     * Genera el contenido Markdown y lo guarda en el directorio actual.
     *
     * @param project proyecto a exportar
     * @param tasks   lista de tareas del proyecto
     * @return ruta del archivo generado
     */
    public Path export(Project project, List<Task> tasks) throws IOException {
        String filename = slugify(project.getName()) + "-tasks.md";
        Path path = Path.of(filename);

        StringBuilder sb = new StringBuilder();
        sb.append("# ").append(project.getName()).append("\n");
        sb.append("_Exportado: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("_\n\n");

        if (project.getDescription() != null && !project.getDescription().isBlank()) {
            sb.append("> ").append(project.getDescription()).append("\n\n");
        }

        // Agrupar por status en orden lógico
        Status[] order = {Status.IN_PROGRESS, Status.TODO, Status.DONE, Status.CANCELLED};
        Map<Status, List<Task>> grouped = tasks.stream().collect(Collectors.groupingBy(Task::getStatus));

        for (Status s : order) {
            List<Task> group = grouped.getOrDefault(s, List.of());
            if (group.isEmpty()) continue;

            sb.append("## ").append(statusLabel(s)).append("\n\n");
            for (Task t : group) {
                sb.append(taskLine(t)).append("\n");
            }
            sb.append("\n");
        }

        Files.writeString(path, sb.toString());
        return path;
    }

    private String taskLine(Task t) {
        String checkbox = switch (t.getStatus()) {
            case DONE      -> "- [x]";
            case CANCELLED -> "- [~]";
            default        -> "- [ ]";
        };
        StringBuilder line = new StringBuilder(checkbox + " **" + t.getTitle() + "**");
        line.append("  `").append(t.getPriority()).append("`");
        if (t.getDueDate() != null) line.append("  vence: ").append(t.getDueDate());
        if (t.getAssignee() != null) line.append("  @").append(t.getAssignee().getUsername());
        if (t.getDescription() != null && !t.getDescription().isBlank()) {
            line.append("\n  > ").append(t.getDescription());
        }
        return line.toString();
    }

    private String statusLabel(Status s) {
        return switch (s) {
            case TODO        -> "📋 Pendientes";
            case IN_PROGRESS -> "⏳ En progreso";
            case DONE        -> "✅ Completadas";
            case CANCELLED   -> "❌ Canceladas";
        };
    }

    private String slugify(String name) {
        return name.toLowerCase()
                   .replaceAll("[^a-z0-9]+", "-")
                   .replaceAll("^-|-$", "");
    }
}

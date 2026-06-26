package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;

import java.time.LocalDate;
import java.util.List;

/**
 * Muestra todos los proyectos y sus tareas en formato árbol, como un 'ls' visual.
 *
 * Uso: ls
 *
 * Ejemplo de salida:
 *
 *   📦 TaskHub — 3 proyectos · 8 tareas
 *
 *   web                          [5 tareas]
 *   ├─ ◉ Refactor auth     HIGH  IN_PROGRESS
 *   ├─ ● Fix bug login     HIGH  TODO        vence hoy ⚠
 *   ├─ ● Añadir tests      MED   TODO
 *   ├─ ✓ Setup CI/CD       LOW   DONE
 *   └─ ✓ Deploy staging    MED   DONE
 */
public class LsCommand implements Command {

    // ANSI
    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String DIM    = "\u001B[2m";
    private static final String RED    = "\u001B[31m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String WHITE  = "\u001B[37m";

    @Override
    public String getName() { return "ls"; }

    @Override
    public String getDescription() { return "Muestra proyectos y tareas en formato árbol"; }

    @Override
    public void execute(CommandContext context) {
        List<Project> projects = context.projectService().findAll();

        if (projects.isEmpty()) {
            context.output().warning("No hay proyectos. Crea uno primero.");
            return;
        }

        int totalTasks = 0;
        for (Project p : projects) {
            totalTasks += context.taskService().findByProjectId(p.getId()).size();
        }

        System.out.println();
        System.out.println(BOLD + "  📦 TaskHub — " + projects.size() + " proyecto(s) · " + totalTasks + " tarea(s)" + RESET);
        System.out.println();

        for (int i = 0; i < projects.size(); i++) {
            Project project = projects.get(i);
            List<Task> tasks = context.taskService().findByProjectId(project.getId());

            // Cabecera del proyecto
            long pending = tasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
            long inProg  = tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
            long done    = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();

            String taskSummary = DIM + "[" + tasks.size() + " tareas" +
                    (inProg > 0 ? " · " + YELLOW + inProg + " en curso" + DIM : "") +
                    (pending > 0 ? " · " + WHITE + pending + " pendientes" + DIM : "") +
                    (done > 0   ? " · " + GREEN  + done   + " hechas"    + DIM : "") +
                    "]" + RESET;

            System.out.println("  " + BOLD + CYAN + "  " + project.getName() + RESET + "  " + taskSummary);

            if (tasks.isEmpty()) {
                System.out.println(DIM + "  │  (sin tareas)" + RESET);
            } else {
                for (int j = 0; j < tasks.size(); j++) {
                    Task task = tasks.get(j);
                    boolean last = (j == tasks.size() - 1);
                    String branch = last ? "  └─ " : "  ├─ ";

                    String icon   = taskIcon(task.getStatus());
                    String pColor = priorityColor(task.getPriority());
                    String sColor = statusColor(task.getStatus());
                    String due    = dueLabel(task);

                    System.out.printf(DIM + branch + RESET + icon + "  %-28s  " + pColor + "%-8s" + RESET + sColor + "%-14s" + RESET + due + "%n",
                            truncate(task.getTitle(), 28),
                            task.getPriority(),
                            task.getStatus());
                }
            }
            System.out.println();
        }
    }

    private String taskIcon(Status status) {
        return switch (status) {
            case DONE        -> GREEN  + "✓" + RESET;
            case IN_PROGRESS -> YELLOW + "◉" + RESET;
            case CANCELLED   -> DIM    + "✗" + RESET;
            default          -> BLUE   + "●" + RESET;
        };
    }

    private String priorityColor(Priority p) {
        return switch (p) {
            case CRITICAL -> RED    + BOLD;
            case HIGH     -> RED;
            case MEDIUM   -> YELLOW;
            case LOW      -> DIM;
        };
    }

    private String statusColor(Status s) {
        return switch (s) {
            case DONE        -> GREEN + DIM;
            case IN_PROGRESS -> YELLOW;
            case CANCELLED   -> DIM;
            default          -> WHITE;
        };
    }

    private String dueLabel(Task task) {
        if (task.getDueDate() == null) return "";
        LocalDate today = LocalDate.now();
        if (task.getDueDate().isBefore(today))  return RED    + "  vencida ⚠"  + RESET;
        if (task.getDueDate().isEqual(today))   return YELLOW + "  vence hoy ⚡" + RESET;
        if (task.getDueDate().isBefore(today.plusDays(3))) return CYAN + "  " + task.getDueDate() + RESET;
        return DIM + "  " + task.getDueDate() + RESET;
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

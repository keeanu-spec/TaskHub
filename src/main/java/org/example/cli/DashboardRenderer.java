package org.example.cli;

import org.example.cli.io.Output;
import org.example.domain.Task;
import org.example.service.ProjectService;
import org.example.service.TaskService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;

/**
 * Renderiza el dashboard de inicio al arrancar TaskHub.
 * Muestra un resumen del día: vencidas, urgentes, en progreso y proyectos activos.
 */
public class DashboardRenderer {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String GREEN  = "\u001B[32m";
    private static final String CYAN   = "\u001B[36m";
    private static final String DIM    = "\u001B[2m";

    private final TaskService taskService;
    private final ProjectService projectService;
    private final Output output;

    public DashboardRenderer(TaskService taskService, ProjectService projectService, Output output) {
        this.taskService    = taskService;
        this.projectService = projectService;
        this.output         = output;
    }

    public void render() {
        List<Task> overdue    = taskService.findOverdue();
        List<Task> dueToday   = taskService.findDueToday();
        List<Task> inProgress = taskService.findInProgress();
        int projects          = projectService.findAll().size();

        LocalDate today = LocalDate.now();
        Locale spanish  = Locale.forLanguageTag("es-ES");
        String dayName  = today.getDayOfWeek().getDisplayName(TextStyle.FULL, spanish);
        String dateStr  = today.format(DateTimeFormatter.ofPattern("d 'de' MMMM yyyy", spanish));
        String header   = "  TaskHub — " + capitalize(dayName) + " " + dateStr + "  ";

        int width = Math.max(header.length() + 2, 50);
        String line = "─".repeat(width);

        System.out.println();
        System.out.println(BOLD + CYAN + "┌" + line + "┐" + RESET);
        System.out.println(BOLD + CYAN + "│" + RESET + BOLD + centerPad(header, width) + CYAN + "│" + RESET);
        System.out.println(BOLD + CYAN + "├" + line + "┤" + RESET);

        printRow(width, RED,    "✖ Vencidas",       overdue.size(),    "tarea(s)");
        printRow(width, YELLOW, "◆ Vencen hoy",     dueToday.size(),   "tarea(s)");
        printRow(width, GREEN,  "▶ En progreso",    inProgress.size(), "tarea(s)");
        printRow(width, CYAN,   "▣ Proyectos activos", projects,        "");

        System.out.println(BOLD + CYAN + "└" + line + "┘" + RESET);
        System.out.println();

        // Mostrar hasta 3 tareas vencidas o urgentes como aviso
        if (!overdue.isEmpty()) {
            System.out.println(RED + BOLD + "  Tareas vencidas:" + RESET);
            overdue.stream().limit(3).forEach(t ->
                System.out.println(RED + "    · " + t.getTitle() +
                    DIM + " [" + t.getProject().getName() + "] " +
                    t.getDueDate() + RESET));
            if (overdue.size() > 3) System.out.println(DIM + "    … y " + (overdue.size() - 3) + " más" + RESET);
            System.out.println();
        }
    }

    private void printRow(int width, String color, String label, int count, String unit) {
        String value = count == 0 ? DIM + "ninguna" + RESET
                                  : color + BOLD + count + RESET + (unit.isBlank() ? "" : "  " + unit);
        String left  = "│  " + color + label + RESET;
        // Pad right side
        int labelLen = stripAnsi(left);
        int valueLen = stripAnsi(value);
        int padding  = width - labelLen - valueLen + 2;
        String pad   = padding > 0 ? " ".repeat(padding) : " ";
        System.out.println(BOLD + CYAN + "│" + RESET + "  " + color + label + RESET + pad + value + "  " + BOLD + CYAN + "│" + RESET);
    }

    private String centerPad(String s, int width) {
        int pad = (width - s.length()) / 2;
        return " ".repeat(Math.max(0, pad)) + s + " ".repeat(Math.max(0, width - s.length() - pad));
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    /** Elimina códigos ANSI para calcular longitud real del texto. */
    private int stripAnsi(String s) {
        return s.replaceAll("\\u001B\\[[;\\d]*m", "").length();
    }
}

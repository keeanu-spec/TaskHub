package org.example.cli.commands.task;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Status;
import org.example.domain.Task;
import org.example.domain.TimeEntry;
import org.example.service.TimeTrackingService;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

public class StatsCommand implements Command {

    private static final String RESET  = "\u001B[0m";
    private static final String BOLD   = "\u001B[1m";
    private static final String CYAN   = "\u001B[36m";
    private static final String GREEN  = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String DIM    = "\u001B[2m";

    @Override public String getName()        { return "Stats"; }
    @Override public String getDescription() { return "Estadísticas de productividad (week / project / tasks)"; }

    @Override
    public void execute(CommandContext context) {
        String sub = context.prompter().prompt("Tipo [week / project / tasks] (enter = week): ").trim().toLowerCase();
        if (sub.isBlank()) sub = "week";

        switch (sub) {
            case "week"    -> showWeek(context);
            case "project" -> showProject(context);
            case "tasks"   -> showTopTasks(context);
            default        -> context.output().error("Opciones: week, project, tasks");
        }
    }

    // ── Resumen semanal ───────────────────────────────────────────────────────
    private void showWeek(CommandContext context) {
        LocalDate today   = LocalDate.now();
        LocalDate monday  = today.with(WeekFields.ISO.dayOfWeek(), 1);
        LocalDate sunday  = monday.plusDays(6);

        List<TimeEntry> entries = context.timeService().findAllCompleted().stream()
            .filter(e -> !e.getStartedAt().toLocalDate().isBefore(monday)
                      && !e.getStartedAt().toLocalDate().isAfter(sunday))
            .toList();

        List<Task> allTasks = context.taskService().findAll();
        long done = allTasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        long totalMinutes = entries.stream().mapToLong(e -> e.getDurationMinutes() != null ? e.getDurationMinutes() : 0).sum();

        System.out.println();
        System.out.println(BOLD + CYAN + "  Esta semana (" + monday + " – " + sunday + ")" + RESET);
        System.out.println(DIM + "  ─────────────────────────────────────" + RESET);
        System.out.println("  " + GREEN + "Tareas completadas:  " + BOLD + done + RESET);
        System.out.println("  " + YELLOW + "Tiempo registrado:   " + BOLD + TimeTrackingService.formatMinutes(totalMinutes) + RESET);
        System.out.println("  " + CYAN + "Sesiones de trabajo: " + BOLD + entries.size() + RESET);

        // Proyecto con más tiempo
        if (!entries.isEmpty()) {
            Map<String, Long> byProject = new HashMap<>();
            for (TimeEntry e : entries) {
                String name = e.getTask().getProject().getName();
                byProject.merge(name, e.getDurationMinutes() != null ? e.getDurationMinutes() : 0, Long::sum);
            }
            String top = byProject.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(en -> en.getKey() + " (" + TimeTrackingService.formatMinutes(en.getValue()) + ")")
                .orElse("—");
            System.out.println("  " + CYAN + "Proyecto más activo: " + BOLD + top + RESET);
        }
        System.out.println();
    }

    // ── Stats por proyecto ────────────────────────────────────────────────────
    private void showProject(CommandContext context) {
        String name = context.prompter().prompt("Nombre del proyecto: ").trim();
        var projectOpt = context.projectService().findAll().stream()
            .filter(p -> p.getName().equalsIgnoreCase(name)).findFirst();

        if (projectOpt.isEmpty()) { context.output().error("Proyecto no encontrado."); return; }

        var project = projectOpt.get();
        List<Task> tasks = context.taskService().findByProjectId(project.getId());

        long todo   = tasks.stream().filter(t -> t.getStatus() == Status.TODO).count();
        long wip    = tasks.stream().filter(t -> t.getStatus() == Status.IN_PROGRESS).count();
        long done   = tasks.stream().filter(t -> t.getStatus() == Status.DONE).count();
        long totalM = tasks.stream().mapToLong(t -> context.timeService().totalMinutes(t.getId())).sum();

        System.out.println();
        System.out.println(BOLD + CYAN + "  Proyecto: " + project.getName() + RESET);
        System.out.println(DIM + "  ─────────────────────────────────────" + RESET);
        System.out.println("  Tareas:       " + tasks.size() + "  (TODO " + todo + "  · WIP " + wip + "  · DONE " + done + ")");
        System.out.println("  Tiempo total: " + BOLD + TimeTrackingService.formatMinutes(totalM) + RESET);
        System.out.println();
    }

    // ── Top tareas por tiempo ─────────────────────────────────────────────────
    private void showTopTasks(CommandContext context) {
        List<Task> tasks = context.taskService().findAll();
        var ranked = tasks.stream()
            .map(t -> Map.entry(t, context.timeService().totalMinutes(t.getId())))
            .filter(e -> e.getValue() > 0)
            .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
            .limit(10)
            .toList();

        if (ranked.isEmpty()) { context.output().warning("Sin tiempo registrado aún."); return; }

        System.out.println();
        System.out.println(BOLD + CYAN + "  Top tareas por tiempo invertido" + RESET);
        System.out.println(DIM + "  ─────────────────────────────────────" + RESET);
        int i = 1;
        for (var entry : ranked) {
            System.out.printf("  %2d.  %-35s  %s%n", i++,
                truncate(entry.getKey().getTitle(), 35),
                BOLD + TimeTrackingService.formatMinutes(entry.getValue()) + RESET);
        }
        System.out.println();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 1) + "…";
    }
}

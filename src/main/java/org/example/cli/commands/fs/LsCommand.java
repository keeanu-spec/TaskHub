package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.VirtualFilesystem;
import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;

import java.time.LocalDate;
import java.util.List;

public class LsCommand implements Command {

    private static final String R  = "\u001B[0m";
    private static final String B  = "\u001B[1m";
    private static final String D  = "\u001B[2m";
    private static final String RD = "\u001B[31m";
    private static final String GR = "\u001B[32m";
    private static final String YL = "\u001B[33m";
    private static final String BL = "\u001B[34m";
    private static final String CY = "\u001B[36m";

    @Override public String getName()        { return "ls"; }
    @Override public String getDescription() { return "Lista el contenido del directorio actual"; }

    @Override public void execute(CommandContext ctx) { execute(ctx, new String[0]); }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (ctx.shell().isInProject()) {
            showTasks(ctx, ctx.shell().currentProject().get());
        } else {
            String path = args.length > 0 ? VirtualFilesystem.join(ctx.shell().dirPath(), args[0])
                                           : ctx.shell().dirPath();
            showDir(ctx, path);
        }
    }

    // ── directorio (carpetas + proyectos) ─────────────────────────────────────
    private void showDir(CommandContext ctx, String path) {
        List<String>  folders  = ctx.vfs().childFolders(path);
        List<Project> projects = ctx.projectService().findByFolderPath(path);

        System.out.println();
        System.out.println(B + "  " + displayPath(path) + R);
        System.out.println(D + "  " + "─".repeat(48) + R);

        if (folders.isEmpty() && projects.isEmpty()) {
            System.out.println(D + "  (vacío)" + R);
        }

        for (String f : folders) {
            System.out.println("  " + CY + B + "d  " + f + "/" + R);
        }
        for (Project p : projects) {
            long tc = ctx.taskService().findByProjectId(p.getId()).size();
            System.out.println("  " + YL + "p  " + p.getName() + R
                + D + "  (" + tc + " tarea" + (tc == 1 ? "" : "s") + ")" + R);
        }
        System.out.println();
        System.out.println(D + "  d=carpeta  p=proyecto  (cd <nombre> para entrar)" + R);
        System.out.println();
    }

    // ── contenido de un proyecto (tareas) ─────────────────────────────────────
    private void showTasks(CommandContext ctx, Project project) {
        List<Task> tasks = ctx.taskService().findByProjectId(project.getId());
        System.out.println();
        System.out.println(B + "  " + ctx.shell().path() + R
            + D + "  [proyecto]" + R);
        System.out.println(D + "  " + "─".repeat(50) + R);
        if (tasks.isEmpty()) {
            System.out.println(D + "  (sin tareas — usa \'touch <título>\' para crear una)" + R);
        } else {
            for (int i = 0; i < tasks.size(); i++) {
                Task t = tasks.get(i);
                String br   = (i == tasks.size()-1) ? "  └─ " : "  ├─ ";
                String icon = icon(t.getStatus());
                String pc   = pc(t.getPriority());
                String due  = due(t);
                System.out.printf(D + br + R + icon + "  " + pc + "%-30s" + R + D + "  %-10s" + R + due + "%n",
                    trunc(t.getTitle(), 30), t.getPriority());
            }
        }
        System.out.println();
    }

    private String displayPath(String p) { return p.equals("/") ? "/" : p; }

    private String icon(Status s) { return switch(s) {
        case DONE        -> GR + "✓" + R;
        case IN_PROGRESS -> YL + "◉" + R;
        case CANCELLED   -> D  + "✗" + R;
        default          -> BL + "●" + R;
    };}
    private String pc(Priority p) { return switch(p) {
        case CRITICAL -> RD + B;
        case HIGH     -> RD;
        case MEDIUM   -> YL;
        case LOW      -> D;
    };}
    private String due(Task t) {
        if (t.getDueDate()==null) return "";
        LocalDate today = LocalDate.now();
        if (t.getDueDate().isBefore(today)) return RD + "  ⚠ vencida" + R;
        if (t.getDueDate().isEqual(today))  return YL + "  ⚡ hoy"     + R;
        return D + "  " + t.getDueDate() + R;
    }
    private String trunc(String s, int n) { return s.length()<=n ? s : s.substring(0,n-1)+"…"; }
}

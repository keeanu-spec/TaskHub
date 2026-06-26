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

public class TreeCommand implements Command {

    private static final String R  = "\u001B[0m";
    private static final String B  = "\u001B[1m";
    private static final String D  = "\u001B[2m";
    private static final String RD = "\u001B[31m";
    private static final String GR = "\u001B[32m";
    private static final String YL = "\u001B[33m";
    private static final String BL = "\u001B[34m";
    private static final String CY = "\u001B[36m";

    @Override public String getName()        { return "tree"; }
    @Override public String getDescription() { return "Árbol completo del filesystem"; }

    @Override
    public void execute(CommandContext ctx) {
        System.out.println();
        System.out.println(B + "/" + R);
        printDir(ctx, "/", "");
        System.out.println();
    }

    private void printDir(CommandContext ctx, String path, String indent) {
        List<String>  folders  = ctx.vfs().childFolders(path);
        List<Project> projects = ctx.projectService().findByFolderPath(path);
        int total = folders.size() + projects.size();
        int i = 0;

        for (String folder : folders) {
            boolean last = (++i == total);
            String branch = last ? "└── " : "├── ";
            String childIndent = indent + (last ? "    " : "│   ");
            System.out.println(indent + branch + CY + B + folder + "/" + R);
            printDir(ctx, VirtualFilesystem.join(path, folder), childIndent);
        }

        for (Project p : projects) {
            boolean last = (++i == total);
            String branch = last ? "└── " : "├── ";
            String childIndent = indent + (last ? "    " : "│   ");
            long tc = ctx.taskService().findByProjectId(p.getId()).size();
            System.out.println(indent + branch + YL + p.getName() + R
                + D + "  (" + tc + " tarea" + (tc==1?"":"s") + ")" + R);
            printTasks(ctx, p, childIndent);
        }
    }

    private void printTasks(CommandContext ctx, Project p, String indent) {
        List<Task> tasks = ctx.taskService().findByProjectId(p.getId());
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            boolean last = i == tasks.size()-1;
            String branch = last ? "└── " : "├── ";
            System.out.println(indent + branch + icon(t.getStatus()) + " "
                + pc(t.getPriority()) + t.getTitle() + R + due(t));
        }
    }

    private String icon(Status s) { return switch(s) {
        case DONE -> GR+"✓"+R; case IN_PROGRESS -> YL+"◉"+R;
        case CANCELLED -> D+"✗"+R; default -> BL+"●"+R; }; }
    private String pc(Priority p) { return switch(p) {
        case CRITICAL -> RD+B; case HIGH -> RD; case MEDIUM -> YL; case LOW -> D; }; }
    private String due(Task t) {
        if (t.getDueDate()==null) return "";
        LocalDate today = LocalDate.now();
        if (t.getDueDate().isBefore(today)) return RD+"  ⚠"+R;
        if (t.getDueDate().isEqual(today))  return YL+"  ⚡"+R;
        return "";
    }
}

package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.VirtualFilesystem;
import org.example.domain.Project;
import org.example.domain.Task;

import java.util.List;
import java.util.Optional;

public class RmCommand implements Command {

    @Override public String getName()        { return "rm"; }
    @Override public String getDescription() { return "Elimina una carpeta, proyecto o tarea"; }
    @Override public void execute(CommandContext ctx) { ctx.output().warning("Uso: rm <nombre>"); }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (args.length == 0) { ctx.output().warning("Uso: rm <nombre>"); return; }
        String name = String.join(" ", args).trim();

        if (ctx.shell().isInProject()) {
            // Eliminar tarea
            deleteTask(ctx, name);
        } else {
            // Primero busca subcarpeta
            String childPath = VirtualFilesystem.join(ctx.shell().dirPath(), name);
            if (ctx.vfs().exists(childPath)) {
                deleteFolder(ctx, childPath, name);
                return;
            }
            // Luego busca proyecto
            Optional<Project> proj = ctx.projectService()
                .findByFolderPath(ctx.shell().dirPath()).stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst();
            if (proj.isPresent()) { deleteProject(ctx, proj.get()); return; }

            ctx.output().error("No encontrado: " + name);
        }
    }

    private void deleteTask(CommandContext ctx, String name) {
        Optional<Task> task = ctx.taskService()
            .findByProjectId(ctx.shell().currentProject().get().getId()).stream()
            .filter(t -> t.getTitle().equalsIgnoreCase(name))
            .findFirst();
        if (task.isEmpty()) { ctx.output().error("Tarea no encontrada: " + name); return; }
        String c = ctx.prompter().prompt("¿Eliminar tarea \"" + task.get().getTitle() + "\"? [s/N]: ").trim();
        if (c.equalsIgnoreCase("s")) {
            ctx.taskService().deleteById(task.get().getId());
            ctx.output().success("Tarea eliminada.");
        } else ctx.output().info("Cancelado.");
    }

    private void deleteProject(CommandContext ctx, Project p) {
        List<Task> tasks = ctx.taskService().findByProjectId(p.getId());
        String c = ctx.prompter().prompt("¿Eliminar proyecto \"" + p.getName()
            + "\" con " + tasks.size() + " tarea(s)? [s/N]: ").trim();
        if (c.equalsIgnoreCase("s")) {
            tasks.forEach(t -> ctx.taskService().deleteById(t.getId()));
            ctx.projectService().deleteById(p.getId());
            ctx.output().success("Proyecto eliminado.");
        } else ctx.output().info("Cancelado.");
    }

    private void deleteFolder(CommandContext ctx, String path, String name) {
        // Solo se puede borrar si está vacía
        List<String> subs = ctx.vfs().childFolders(path);
        List<Project> projs = ctx.projectService().findByFolderPath(path);
        if (!subs.isEmpty() || !projs.isEmpty()) {
            ctx.output().error("La carpeta \"" + name + "\" no está vacía. Borra su contenido primero.");
            return;
        }
        String c = ctx.prompter().prompt("¿Eliminar carpeta \"" + name + "\"? [s/N]: ").trim();
        // VirtualFilesystem no tiene delete aún — lo añadimos
        if (c.equalsIgnoreCase("s")) {
            ctx.vfs().rmdir(path);
            ctx.output().success("Carpeta eliminada: " + path);
        } else ctx.output().info("Cancelado.");
    }
}

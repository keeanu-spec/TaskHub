package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.VirtualFilesystem;
import org.example.domain.Project;

import java.util.List;

public class CdCommand implements Command {

    @Override public String getName()        { return "cd"; }
    @Override public String getDescription() { return "Navega: cd <carpeta|proyecto>  |  cd ..  |  cd /"; }

    @Override public void execute(CommandContext ctx) { ctx.output().warning("Uso: cd <nombre> | cd .. | cd /"); }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (args.length == 0 || args[0].equals("/")) {
            ctx.shell().goRoot();
            ctx.output().info("→ /");
            return;
        }
        if (args[0].equals("..")) {
            ctx.shell().goUp();
            ctx.output().info("→ " + ctx.shell().path());
            return;
        }

        String target = args[0];

        if (ctx.shell().isInProject()) {
            ctx.output().warning("Ya estás dentro de un proyecto. Usa \'cd ..\'  para salir.");
            return;
        }

        String currentDir = ctx.shell().dirPath();

        // ¿Es una subcarpeta?
        String childPath = VirtualFilesystem.join(currentDir, target);
        if (ctx.vfs().exists(childPath)) {
            ctx.shell().enterFolder(childPath);
            ctx.output().info("→ " + childPath);
            return;
        }

        // ¿Es un proyecto en este directorio?
        List<Project> projects = ctx.projectService().findByFolderPath(currentDir);
        Project found = projects.stream()
            .filter(p -> p.getName().equalsIgnoreCase(target))
            .findFirst().orElse(null);

        if (found != null) {
            ctx.shell().enterProject(found);
            ctx.output().info("→ " + ctx.shell().path());
            return;
        }

        ctx.output().error("No existe \'" + target + "\' en " + currentDir
            + ". Usa \'ls\' para ver el contenido.");
    }
}

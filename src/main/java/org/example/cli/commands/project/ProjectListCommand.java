package org.example.cli.commands.project;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Project;

import java.util.List;

public class ProjectListCommand implements Command {

    @Override public String getName()        { return "Project-List"; }
    @Override public String getDescription() { return "Lista todos los proyectos"; }

    @Override
    public void execute(CommandContext context) {
        List<Project> projects = context.projectService().findAll();
        if (projects.isEmpty()) {
            context.output().warning("No hay proyectos. Usa 'Project-Create' para crear uno.");
            return;
        }
        context.output().info(String.format("%-36s  %-20s  %s", "ID", "Nombre", "Propietario"));
        context.output().info("─".repeat(70));
        for (Project p : projects) {
            context.output().print(String.format("%-36s  %-20s  %s",
                p.getId(), p.getName(), p.getOwner().getUsername()));
        }
    }
}

package org.example.cli.commands.project;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Project;
import org.example.domain.User;

public class ProjectCreateCommand implements Command {

    @Override public String getName()        { return "Project-Create"; }
    @Override public String getDescription() { return "Crea un nuevo proyecto"; }

    @Override
    public void execute(CommandContext context) {
        String name  = context.prompter().prompt("Nombre del proyecto: ").trim();
        String desc  = context.prompter().prompt("Descripción (opcional): ").trim();
        String owner = context.prompter().prompt("Email del propietario: ").trim();

        User ownerUser = context.userService().findByEmail(owner)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + owner));

        Project project = context.projectService().create(name, desc.isBlank() ? null : desc, ownerUser);
        context.output().success("Proyecto creado: " + project.getName() + " [" + project.getId() + "]");
    }
}

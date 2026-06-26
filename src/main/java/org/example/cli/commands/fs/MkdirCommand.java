package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.cli.VirtualFilesystem;

public class MkdirCommand implements Command {

    @Override public String getName()        { return "mkdir"; }
    @Override public String getDescription() { return "Crea una carpeta (mkdir <nombre>)"; }

    @Override public void execute(CommandContext ctx) { ctx.output().warning("Uso: mkdir <nombre>"); }

    @Override
    public void execute(CommandContext ctx, String[] args) {
        if (args.length == 0) { ctx.output().warning("Uso: mkdir <nombre>"); return; }

        if (ctx.shell().isInProject()) {
            ctx.output().error("No puedes crear carpetas dentro de un proyecto. Sal con \'cd ..\'");
            return;
        }

        String name = args[0].trim();
        if (name.isBlank()) { ctx.output().warning("El nombre no puede estar vacío."); return; }

        String newPath = VirtualFilesystem.join(ctx.shell().dirPath(), name);

        if (ctx.vfs().exists(newPath)) {
            ctx.output().warning("Ya existe la carpeta: " + newPath);
            return;
        }

        ctx.vfs().mkdir(newPath);
        ctx.output().success("Carpeta creada: " + newPath);
    }
}

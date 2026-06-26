package org.example.cli.commands.fs;

import org.example.cli.Command;
import org.example.cli.CommandContext;

public class PwdCommand implements Command {

    @Override public String getName()        { return "pwd"; }
    @Override public String getDescription() { return "Muestra la ruta actual"; }

    @Override
    public void execute(CommandContext ctx) {
        ctx.output().info(ctx.shell().path());
    }
}

package org.example.cli.commands.user;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.User;

public class ListUsersCommand implements Command {

    public String getName() {
        return "User-List";
    }

    public String getDescription() {
        return "List of Users";
    }

    public void execute(CommandContext context) {
        var users = context.userService().findAll();
        if (users.isEmpty()) {
            context.output().info("No hay usuarios registrados.");
            return;
        }
        for (User user : users) {
            context.output().info(user.getUsername() + " - " + user.getEmail());
        }
    }
}

package org.example.cli.commands.user;

import org.example.cli.Command;
import org.example.cli.CommandContext;
import org.example.domain.Role;

public class CreateUserCommand implements Command {

    public String getName() {
        return "User-add";
    }

    public String getDescription() {
        return "Create new User";
    }

    public void execute(CommandContext context) {
        String username = context.prompter().prompt("Username: ");
        String email    = context.prompter().prompt("Email: ");
        String password = context.prompter().promptPassword("Password: ");
        context.userService().create(username, email, password, Role.MEMBER);
        context.output().success("User created successfully");
    }
}

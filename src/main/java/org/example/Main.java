package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.example.Config.JpaConfig;
import org.example.cli.CommandContext;
import org.example.cli.CommandRegistry;
import org.example.cli.DashboardRenderer;
import org.example.cli.LoadingScreen;
import org.example.cli.OpenNotepadCommand;
import org.example.cli.TaskHubShell;
import org.example.cli.commands.ClearCommand;
import org.example.cli.commands.DiscordCommand;
import org.example.cli.commands.ExitCommand;
import org.example.cli.commands.HelpCommand;
import org.example.cli.commands.task.ExportProjectCommand;
import org.example.cli.commands.task.TaskFilterCommand;
import org.example.cli.commands.user.CreateUserCommand;
import org.example.cli.commands.user.ListUsersCommand;
import org.example.cli.io.Output;
import org.example.cli.io.Prompter;
import org.example.repository.jpa.JpaProjectRepository;
import org.example.repository.jpa.JpaTaskRepository;
import org.example.repository.jpa.JpaUserRepository;
import org.example.service.ProjectService;
import org.example.service.TaskService;
import org.example.service.UserService;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        // ── Pantalla de carga mientras JPA inicializa ──────────────────────
        LoadingScreen loading = new LoadingScreen();
        loading.start();

        Terminal terminal     = TerminalBuilder.terminal();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        EntityManagerFactory emf = JpaConfig.getEntityManagerFactory(); // <-- parte lenta

        loading.stop(); // limpia pantalla cuando JPA ya está listo

        // ── Repositorios y servicios ───────────────────────────────────────
        JpaUserRepository    userRepository    = new JpaUserRepository(emf);
        JpaProjectRepository projectRepository = new JpaProjectRepository(emf);
        JpaTaskRepository    taskRepository    = new JpaTaskRepository(emf);

        UserService    userService    = new UserService(userRepository);
        ProjectService projectService = new ProjectService(projectRepository);
        TaskService    taskService    = new TaskService(taskRepository);

        Output   output   = new Output();
        Prompter prompter = new Prompter(lineReader);

        CommandRegistry registry = new CommandRegistry();
        CommandContext  ctx      = new CommandContext(userService, projectService, taskService, output, prompter, registry);

        // ── Comandos ───────────────────────────────────────────────────────
        registry.register(new HelpCommand());
        registry.register(new ExitCommand());
        registry.register(new ClearCommand());
        registry.register(new DiscordCommand());
        registry.register(new OpenNotepadCommand());
        registry.register(new CreateUserCommand());
        registry.register(new ListUsersCommand());
        registry.register(new TaskFilterCommand());
        registry.register(new ExportProjectCommand());

        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::close));

        // ── Shell ──────────────────────────────────────────────────────────
        DashboardRenderer dashboard = new DashboardRenderer(taskService, projectService, output);
        new TaskHubShell(lineReader, ctx, registry, dashboard).run();
    }
}

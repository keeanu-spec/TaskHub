package org.example;

import jakarta.persistence.EntityManagerFactory;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import org.example.Config.JpaConfig;
import org.example.Config.FlywayConfig;
import org.example.cli.CommandContext;
import org.example.cli.ShellState;
import org.example.cli.VirtualFilesystem;
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
import org.example.cli.commands.task.NoteAddCommand;
import org.example.cli.commands.task.NoteListCommand;
import org.example.cli.commands.task.StatsCommand;
import org.example.cli.commands.task.TaskCreateCommand;
import org.example.cli.commands.task.TaskFilterCommand;
import org.example.cli.commands.task.TaskStartCommand;
import org.example.cli.commands.task.TaskStopCommand;
import org.example.cli.commands.task.TaskTimeCommand;
import org.example.cli.commands.user.CreateUserCommand;
import org.example.cli.commands.user.ListUsersCommand;
import org.example.cli.commands.fs.*;
import org.example.cli.commands.project.ProjectCreateCommand;
import org.example.cli.commands.project.ProjectListCommand;
import org.example.cli.commands.task.TaskCreateCommand;
import org.example.cli.io.Output;
import org.example.cli.io.Prompter;
import org.example.repository.jpa.*;
import org.example.service.*;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {

        LoadingScreen loading = new LoadingScreen();
        loading.start();

        // Migraciones de BD — siempre antes de JPA
        FlywayConfig.migrate();

        Terminal terminal     = TerminalBuilder.terminal();
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();
        EntityManagerFactory emf = JpaConfig.getEntityManagerFactory();

        loading.stop();

        // ── Repositorios ───────────────────────────────────────────────────
        JpaUserRepository        userRepo     = new JpaUserRepository(emf);
        JpaProjectRepository     projectRepo  = new JpaProjectRepository(emf);
        JpaTaskRepository        taskRepo     = new JpaTaskRepository(emf);
        JpaTaskNoteRepository    noteRepo     = new JpaTaskNoteRepository(emf);
        JpaTimeEntryRepository   timeRepo     = new JpaTimeEntryRepository(emf);

        // ── Servicios ──────────────────────────────────────────────────────
        UserService          userService  = new UserService(userRepo);
        ProjectService       projectSvc   = new ProjectService(projectRepo);
        TaskService          taskSvc      = new TaskService(taskRepo);
        TaskNoteService      noteSvc      = new TaskNoteService(noteRepo);
        TimeTrackingService  timeSvc      = new TimeTrackingService(timeRepo);

        Output   output   = new Output();
        Prompter prompter = new Prompter(lineReader);

        CommandRegistry   registry = new CommandRegistry();
        ShellState        shell    = new ShellState();
        VirtualFilesystem vfs      = new VirtualFilesystem();
        CommandContext  ctx      = new CommandContext(
            userService, projectSvc, taskSvc, noteSvc, timeSvc, output, prompter, registry, shell, vfs);

        // ── Comandos generales ─────────────────────────────────────────────
        registry.register(new HelpCommand());
        registry.register(new ExitCommand());
        registry.register(new ClearCommand());
        registry.register(new DiscordCommand());
        registry.register(new OpenNotepadCommand());

        // ── Usuario ────────────────────────────────────────────────────────
        registry.register(new CreateUserCommand());
        registry.register(new ListUsersCommand());

        // ── Proyectos ──────────────────────────────────────────────────────
        registry.register(new ProjectCreateCommand());
        registry.register(new ProjectListCommand());

        // ── Filesystem virtual ────────────────────────────────────────────
        registry.register(new org.example.cli.commands.fs.LsCommand());
        registry.register(new CdCommand());
        registry.register(new PwdCommand());
        registry.register(new MkdirCommand());
        registry.register(new TouchCommand());
        registry.register(new RmCommand());
        registry.register(new CatCommand());
        registry.register(new TreeCommand());

        // ── Tareas — Fase A ────────────────────────────────────────────────
        registry.register(new TaskCreateCommand());
        registry.register(new TaskFilterCommand());
        registry.register(new ExportProjectCommand());

        // ── Tareas — Fase B ────────────────────────────────────────────────
        registry.register(new NoteAddCommand());
        registry.register(new NoteListCommand());
        registry.register(new TaskStartCommand());
        registry.register(new TaskStopCommand());
        registry.register(new TaskTimeCommand());
        registry.register(new StatsCommand());

        Runtime.getRuntime().addShutdownHook(new Thread(JpaConfig::close));

        DashboardRenderer dashboard = new DashboardRenderer(taskSvc, projectSvc, output);
        new TaskHubShell(lineReader, ctx, registry, dashboard).run();
    }
}

package org.example.cli;

import org.example.cli.io.Output;
import org.example.cli.io.Prompter;
import org.example.service.ProjectService;
import org.example.service.TaskNoteService;
import org.example.service.TaskService;
import org.example.service.TimeTrackingService;
import org.example.service.UserService;

public record CommandContext(
        UserService userService,
        ProjectService projectService,
        TaskService taskService,
        TaskNoteService noteService,
        TimeTrackingService timeService,
        Output output,
        Prompter prompter,
        CommandRegistry commandRegistry) {
}

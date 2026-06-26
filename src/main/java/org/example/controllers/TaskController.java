package org.example.controllers;

import jakarta.validation.Valid;
import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;
import org.example.domain.User;
import org.example.dto.CreateTaskRequest;
import org.example.dto.TaskResponse;
import org.example.dto.UpdateTaskStatusRequest;
import org.example.service.ProjectService;
import org.example.service.TaskService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects/{projectId}/tasks")
public class TaskController {

    private final TaskService taskService;
    private final ProjectService projectService;
    private final UserService userService;

    public TaskController(TaskService taskService, ProjectService projectService, UserService userService) {
        this.taskService = taskService;
        this.projectService = projectService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(
            @PathVariable UUID projectId,
            @Valid @RequestBody CreateTaskRequest request,
            Authentication authentication) {
        Project project = projectService.findById(projectId);
        User assignee = null;
        if (request.getAssigneeId() != null) {
            assignee = userService.findById(request.getAssigneeId());
        }
        Priority priority = request.getPriority() != null
                ? Priority.valueOf(request.getPriority().toUpperCase())
                : Priority.MEDIUM;
        Task task = taskService.create(
                request.getTitle(), assignee, request.getDescription(),
                Status.TODO, priority, project, request.getDueDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskResponse.from(task));
    }

    @GetMapping
    public ResponseEntity<List<TaskResponse>> listTasks(@PathVariable UUID projectId) {
        List<TaskResponse> tasks = taskService.findByProjectId(projectId).stream()
                .map(TaskResponse::from)
                .toList();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable UUID projectId, @PathVariable UUID id) {
        return ResponseEntity.ok(TaskResponse.from(taskService.findById(id)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateStatus(
            @PathVariable UUID projectId,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        Status newStatus = Status.valueOf(request.getStatus().toUpperCase());
        Task updated = taskService.updateStatus(id, newStatus);
        return ResponseEntity.ok(TaskResponse.from(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID projectId, @PathVariable UUID id) {
        taskService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

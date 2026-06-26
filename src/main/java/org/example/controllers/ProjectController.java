package org.example.controllers;

import jakarta.validation.Valid;
import org.example.domain.User;
import org.example.dto.AddMemberRequest;
import org.example.dto.CreateProjectRequest;
import org.example.dto.ProjectResponse;
import org.example.service.ProjectService;
import org.example.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService, UserService userService) {
        this.projectService = projectService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(
            @Valid @RequestBody CreateProjectRequest request,
            Authentication authentication) {
        User owner = getCurrentUser(authentication);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ProjectResponse.from(
                        projectService.create(request.getName(), request.getDescription(), owner)));
    }

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> listProjects(Authentication authentication) {
        User currentUser = getCurrentUser(authentication);
        List<ProjectResponse> projects = projectService.findByOwnerId(currentUser.getId()).stream()
                .map(ProjectResponse::from)
                .toList();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable UUID id) {
        return ResponseEntity.ok(ProjectResponse.from(projectService.findById(id)));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(
            @PathVariable UUID id,
            @Valid @RequestBody AddMemberRequest request) {
        User newMember = userService.findById(request.getUserId());
        projectService.addMember(id, newMember);
        return ResponseEntity.ok(ProjectResponse.from(projectService.findById(id)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable UUID id) {
        projectService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return userService.findByEmail(authentication.getName()).orElseThrow();
    }
}

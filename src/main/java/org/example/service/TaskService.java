package org.example.service;

import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;
import org.example.domain.User;
import org.example.exception.EntityNotFoundException;
import org.example.exception.ValidationException;
import org.example.repository.TaskRepositoryPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepositoryPort taskRepository;

    public TaskService(TaskRepositoryPort taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task create(String title, User assignee, String description,
                       Status status, Priority priority, Project project, LocalDate dueDate) {
        if (title.isBlank()) throw new ValidationException("El título no puede estar vacío");
        return taskRepository.save(new Task(title, assignee, description, status, priority, project, dueDate));
    }

    public List<Task> findAll() { return taskRepository.findAll(); }

    public List<Task> findByProjectId(UUID projectId) { return taskRepository.findByProjectId(projectId); }

    public Task findById(UUID id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Tarea no encontrada: " + id));
    }

    public Task assign(User user, Task task) {
        if (!task.getProject().getMembers().contains(user))
            throw new ValidationException("El usuario no es miembro del proyecto");
        task.setAssignee(user);
        return taskRepository.save(task);
    }

    public Task updateStatus(UUID taskId, Status newStatus) {
        Task task = findById(taskId);
        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    public void deleteById(UUID id) {
        if (!taskRepository.existsById(id)) throw new EntityNotFoundException("Tarea no encontrada: " + id);
        taskRepository.deleteById(id);
    }

    // ── Filtros ───────────────────────────────────────────────────────────────

    /**
     * Filtra tareas en memoria. Todos los parámetros son opcionales (null = sin filtro).
     *
     * @param projectName    nombre parcial del proyecto (case-insensitive)
     * @param priority       prioridad exacta
     * @param status         estado exacto
     * @param dueTodayOnly   true → solo tareas que vencen hoy
     */
    public List<Task> findByFilters(String projectName, Priority priority, Status status, boolean dueTodayOnly) {
        return taskRepository.findAll().stream()
                .filter(t -> projectName == null || t.getProject().getName()
                        .toLowerCase().contains(projectName.toLowerCase()))
                .filter(t -> priority == null || t.getPriority() == priority)
                .filter(t -> status == null || t.getStatus() == status)
                .filter(t -> !dueTodayOnly || (t.getDueDate() != null && t.getDueDate().isEqual(LocalDate.now())))
                .collect(Collectors.toList());
    }

    /** Tareas que vencen hoy y no están completadas. */
    public List<Task> findDueToday() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isEqual(today))
                .filter(t -> t.getStatus() != Status.DONE && t.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());
    }

    /** Tareas vencidas (dueDate < hoy) y no completadas. */
    public List<Task> findOverdue() {
        LocalDate today = LocalDate.now();
        return taskRepository.findAll().stream()
                .filter(t -> t.getDueDate() != null && t.getDueDate().isBefore(today))
                .filter(t -> t.getStatus() != Status.DONE && t.getStatus() != Status.CANCELLED)
                .collect(Collectors.toList());
    }

    /** Tareas en progreso. */
    public List<Task> findInProgress() {
        return taskRepository.findAll().stream()
                .filter(t -> t.getStatus() == Status.IN_PROGRESS)
                .collect(Collectors.toList());
    }
}

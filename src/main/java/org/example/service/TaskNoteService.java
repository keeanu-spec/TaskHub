package org.example.service;

import org.example.domain.Task;
import org.example.domain.TaskNote;
import org.example.repository.jpa.JpaTaskNoteRepository;

import java.util.List;
import java.util.UUID;

public class TaskNoteService {

    private final JpaTaskNoteRepository repo;

    public TaskNoteService(JpaTaskNoteRepository repo) {
        this.repo = repo;
    }

    public TaskNote addNote(Task task, String content) {
        if (content.isBlank()) throw new IllegalArgumentException("La nota no puede estar vacía");
        return repo.save(new TaskNote(task, content));
    }

    public List<TaskNote> findByTaskId(UUID taskId) {
        return repo.findByTaskId(taskId);
    }
}

package org.example.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "task_notes")
public class TaskNote {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected TaskNote() {}

    public TaskNote(Task task, String content) {
        this.id        = UUID.randomUUID();
        this.task      = task;
        this.content   = content;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId()              { return id; }
    public Task getTask()            { return task; }
    public String getContent()       { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setContent(String c) { this.content = c; }
}

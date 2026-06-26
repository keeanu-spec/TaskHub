package org.example.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "time_entries")
public class TimeEntry {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;        // null = timer activo

    @Column(name = "duration_minutes")
    private Long durationMinutes;         // calculado al parar

    protected TimeEntry() {}

    public TimeEntry(Task task) {
        this.id        = UUID.randomUUID();
        this.task      = task;
        this.startedAt = LocalDateTime.now();
    }

    /** Para el timer y calcula la duración. */
    public void stop() {
        this.endedAt         = LocalDateTime.now();
        this.durationMinutes = java.time.Duration.between(startedAt, endedAt).toMinutes();
    }

    public boolean isActive()               { return endedAt == null; }
    public UUID getId()                     { return id; }
    public Task getTask()                   { return task; }
    public LocalDateTime getStartedAt()     { return startedAt; }
    public LocalDateTime getEndedAt()       { return endedAt; }
    public Long getDurationMinutes()        { return durationMinutes; }
}

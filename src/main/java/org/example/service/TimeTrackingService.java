package org.example.service;

import org.example.domain.Task;
import org.example.domain.TimeEntry;
import org.example.repository.jpa.JpaTimeEntryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TimeTrackingService {

    private final JpaTimeEntryRepository repo;

    public TimeTrackingService(JpaTimeEntryRepository repo) {
        this.repo = repo;
    }

    /** Inicia timer para una tarea. Lanza excepción si ya hay uno activo. */
    public TimeEntry start(Task task) {
        Optional<TimeEntry> active = repo.findAnyActive();
        if (active.isPresent()) {
            Task activeTask = active.get().getTask();
            throw new IllegalStateException(
                "Ya hay un timer activo en: \"" + activeTask.getTitle() + "\" — páralo primero con Task-Stop");
        }
        return repo.save(new TimeEntry(task));
    }

    /** Para el timer activo de una tarea y devuelve la entrada con duración. */
    public TimeEntry stop(UUID taskId) {
        TimeEntry entry = repo.findActiveByTaskId(taskId)
            .orElseThrow(() -> new IllegalStateException("No hay timer activo para esa tarea"));
        entry.stop();
        return repo.save(entry);
    }

    /** Para cualquier timer activo (sin importar la tarea). */
    public Optional<TimeEntry> stopAny() {
        return repo.findAnyActive().map(entry -> {
            entry.stop();
            return repo.save(entry);
        });
    }

    /** Tiempo total invertido en una tarea (minutos). */
    public long totalMinutes(UUID taskId) {
        return repo.findByTaskId(taskId).stream()
            .mapToLong(e -> e.getDurationMinutes() != null ? e.getDurationMinutes() : 0)
            .sum();
    }

    /** Entradas completadas de una tarea. */
    public List<TimeEntry> findByTaskId(UUID taskId) {
        return repo.findByTaskId(taskId);
    }

    /** Todas las entradas (para estadísticas). */
    public List<TimeEntry> findAllCompleted() {
        return repo.findAllCompleted();
    }

    /** Timer activo ahora mismo, si hay uno. */
    public Optional<TimeEntry> findActive() {
        return repo.findAnyActive();
    }

    // ── Utilidad: formatear minutos como "2h 15m" ─────────────────────────
    public static String formatMinutes(long minutes) {
        if (minutes <= 0) return "0m";
        long h = minutes / 60;
        long m = minutes % 60;
        if (h == 0) return m + "m";
        if (m == 0) return h + "h";
        return h + "h " + m + "m";
    }
}

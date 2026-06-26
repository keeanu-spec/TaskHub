package org.example.repository.jpa;

import jakarta.persistence.*;
import org.example.domain.TimeEntry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaTimeEntryRepository {

    private final EntityManagerFactory emf;

    public JpaTimeEntryRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public TimeEntry save(TimeEntry entry) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            TimeEntry merged = em.merge(entry);
            em.getTransaction().commit();
            return merged;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    /** Timer activo para una tarea (endedAt IS NULL). */
    public Optional<TimeEntry> findActiveByTaskId(UUID taskId) {
        EntityManager em = emf.createEntityManager();
        try {
            List<TimeEntry> result = em.createQuery(
                "SELECT e FROM TimeEntry e WHERE e.task.id = :taskId AND e.endedAt IS NULL",
                TimeEntry.class)
                .setParameter("taskId", taskId)
                .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /** Cualquier timer activo en cualquier tarea. */
    public Optional<TimeEntry> findAnyActive() {
        EntityManager em = emf.createEntityManager();
        try {
            List<TimeEntry> result = em.createQuery(
                "SELECT e FROM TimeEntry e WHERE e.endedAt IS NULL",
                TimeEntry.class)
                .setMaxResults(1)
                .getResultList();
            return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
        } finally {
            em.close();
        }
    }

    /** Todas las entradas de una tarea (completadas). */
    public List<TimeEntry> findByTaskId(UUID taskId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT e FROM TimeEntry e WHERE e.task.id = :taskId AND e.endedAt IS NOT NULL ORDER BY e.startedAt DESC",
                TimeEntry.class)
                .setParameter("taskId", taskId)
                .getResultList();
        } finally {
            em.close();
        }
    }

    /** Todas las entradas completadas (para estadísticas). */
    public List<TimeEntry> findAllCompleted() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT e FROM TimeEntry e WHERE e.endedAt IS NOT NULL",
                TimeEntry.class)
                .getResultList();
        } finally {
            em.close();
        }
    }
}

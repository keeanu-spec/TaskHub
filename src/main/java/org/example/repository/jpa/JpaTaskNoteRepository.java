package org.example.repository.jpa;

import jakarta.persistence.*;
import org.example.domain.Task;
import org.example.domain.TaskNote;

import java.util.List;
import java.util.UUID;

public class JpaTaskNoteRepository {

    private final EntityManagerFactory emf;

    public JpaTaskNoteRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public TaskNote save(TaskNote note) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(note);
            em.getTransaction().commit();
            return note;
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public List<TaskNote> findByTaskId(UUID taskId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT n FROM TaskNote n WHERE n.task.id = :taskId ORDER BY n.createdAt ASC",
                TaskNote.class)
                .setParameter("taskId", taskId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}

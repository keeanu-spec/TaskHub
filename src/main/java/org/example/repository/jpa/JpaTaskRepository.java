package org.example.repository.jpa;

import jakarta.persistence.*;
import org.example.domain.Task;
import org.example.repository.Repository;
import org.example.repository.TaskRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaTaskRepository implements Repository<Task, UUID>, TaskRepositoryPort {

    private final EntityManagerFactory emf;

    public JpaTaskRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Task save(Task task) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(task);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Task.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Task> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT t FROM Task t", Task.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Task task = em.find(Task.class, id);
            if (task != null) em.remove(task);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public boolean existsById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Task.class, id) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Task> findByProjectId(UUID projectId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT t FROM Task t WHERE t.project.id = :projectId", Task.class)
                .setParameter("projectId", projectId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}

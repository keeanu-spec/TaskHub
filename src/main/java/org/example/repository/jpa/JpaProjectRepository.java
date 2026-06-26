package org.example.repository.jpa;

import jakarta.persistence.*;
import org.example.domain.Project;
import org.example.repository.Repository;
import org.example.repository.ProjectRepositoryPort;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaProjectRepository implements Repository<Project, UUID>, ProjectRepositoryPort {

    private final EntityManagerFactory emf;

    public JpaProjectRepository(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Project save(Project project) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(project);
            em.getTransaction().commit();
        } catch (Exception e) {
            em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
        return project;
    }

    @Override
    public Optional<Project> findById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            return Optional.ofNullable(em.find(Project.class, id));
        } finally {
            em.close();
        }
    }

    @Override
    public List<Project> findAll() {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery("SELECT p FROM Project p", Project.class).getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void deleteById(UUID id) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Project project = em.find(Project.class, id);
            if (project != null) em.remove(project);
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
            return em.find(Project.class, id) != null;
        } finally {
            em.close();
        }
    }

    @Override
    public List<Project> findByFolderPath(String folderPath) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Project p WHERE p.folderPath = :fp", Project.class)
                .setParameter("fp", folderPath)
                .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Project> findByOwnerId(UUID ownerId) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.createQuery(
                "SELECT p FROM Project p WHERE p.owner.id = :ownerId", Project.class)
                .setParameter("ownerId", ownerId)
                .getResultList();
        } finally {
            em.close();
        }
    }
}

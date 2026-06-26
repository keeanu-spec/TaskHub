package org.example.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.*;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    /** Ruta de la carpeta virtual donde vive este proyecto. Por defecto "/". */
    @Column(name = "folder_path", nullable = false, length = 500,
            columnDefinition = "VARCHAR(500) DEFAULT '/'")
    private String folderPath = "/";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "project_members",
        joinColumns = @JoinColumn(name = "project_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @OneToMany(mappedBy = "project", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Task> tasks = new ArrayList<>();

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected Project() {}

    public Project(String name, String description, User owner) {
        this.id          = UUID.randomUUID();
        this.name        = name;
        this.description = description;
        this.owner       = owner;
        this.folderPath  = "/";
        this.members     = new HashSet<>();
        this.tasks       = new ArrayList<>();
        this.createdAt   = LocalDateTime.now();
    }

    public UUID getId()                   { return id; }
    public void setId(UUID id)            { this.id = id; }

    public String getName()               { return name; }
    public void setName(String name)      { this.name = name; }

    public String getDescription()                        { return description; }
    public void setDescription(String description)        { this.description = description; }

    public String getFolderPath()                         { return folderPath; }
    public void setFolderPath(String folderPath)          { this.folderPath = folderPath; }

    public User getOwner()                { return owner; }
    public void setOwner(User owner)      { this.owner = owner; }

    public Set<User> getMembers()                   { return members; }
    public void setMembers(Set<User> members)       { this.members = members; }

    public List<Task> getTasks()                    { return tasks; }
    public void setTasks(List<Task> tasks)          { this.tasks = tasks; }

    public LocalDateTime getCreatedAt()                           { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt)             { this.createdAt = createdAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Project)) return false;
        Project p = (Project) o;
        return name != null && name.equals(p.name);
    }

    @Override
    public int hashCode() { return name != null ? name.hashCode() : 0; }
}

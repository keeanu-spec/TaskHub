package org.example.repository.jdbc;

import java.sql.*;
import java.sql.Date;
import java.util.*;
import javax.sql.DataSource;
import org.example.domain.Priority;
import org.example.domain.Project;
import org.example.domain.Status;
import org.example.domain.Task;
import org.example.domain.User;

public class TaskJdbcRepository extends JdbcRepository<Task, UUID> {

    private final UserJdbcRepository userRepository;
    private final ProjectJdbcRepository projectRepository;

    public TaskJdbcRepository(DataSource dataSource) {
        super(dataSource);
        this.userRepository = new UserJdbcRepository(dataSource);
        this.projectRepository = new ProjectJdbcRepository(dataSource);
    }

    @Override
    public Task save(Task task) {
        String sql = "MERGE INTO tasks (id, title, description, status, priority, assignee_id, project_id, due_date, created_at) KEY(id) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, task.getId());
            ps.setString(2, task.getTitle());
            ps.setString(3, task.getDescription());
            ps.setString(4, task.getStatus().name());
            ps.setString(5, task.getPriority().name());
            ps.setObject(6, task.getAssignee() != null ? task.getAssignee().getId() : null);
            ps.setObject(7, task.getProject().getId());
            ps.setDate(8, task.getDueDate() != null ? Date.valueOf(task.getDueDate()) : null);
            ps.setTimestamp(9, Timestamp.valueOf(task.getCreatedAt()));
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return task;
    }

    @Override
    public Optional<Task> findById(UUID id) {
        String sql = "SELECT * FROM tasks WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @Override
    public List<Task> findAll() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT * FROM tasks";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tasks.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tasks;
    }

    @Override
    public void deleteById(UUID id) {
        String sql = "DELETE FROM tasks WHERE id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, id);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Task mapRow(ResultSet rs) throws SQLException {
        UUID assigneeId = rs.getObject("assignee_id", UUID.class);
        User assignee = assigneeId != null ? userRepository.findById(assigneeId).orElse(null) : null;
        UUID projectId = rs.getObject("project_id", UUID.class);
        Project project = projectRepository.findById(projectId).orElseThrow();

        Task task = new Task(
            rs.getString("title"),
            assignee,
            rs.getString("description"),
            Status.valueOf(rs.getString("status")),
            Priority.valueOf(rs.getString("priority")),
            project,
            rs.getDate("due_date") != null ? rs.getDate("due_date").toLocalDate() : null
        );
        task.setId(rs.getObject("id", UUID.class));
        task.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return task;
    }
}

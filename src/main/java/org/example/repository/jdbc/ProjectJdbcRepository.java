package org.example.repository.jdbc;

import java.sql.*;
import java.util.*;
import javax.sql.DataSource;
import org.example.domain.Project;
import org.example.domain.User;

public class ProjectJdbcRepository extends JdbcRepository<Project, UUID> {

    private final UserJdbcRepository userRepository;

    public ProjectJdbcRepository(DataSource dataSource) {
        super(dataSource);
        this.userRepository = new UserJdbcRepository(dataSource);
    }

    @Override
    public Project save(Project project) {
        String sql = "MERGE INTO projects (id, name, description, owner_id, created_at) KEY(id) VALUES (?,?,?,?,?)";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, project.getId());
            ps.setString(2, project.getName());
            ps.setString(3, project.getDescription());
            ps.setObject(4, project.getOwner().getId());
            ps.setTimestamp(5, Timestamp.valueOf(project.getCreatedAt()));
            ps.executeUpdate();
            saveMembers(project);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return project;
    }

    @Override
    public Optional<Project> findById(UUID id) {
        String sql = "SELECT * FROM projects WHERE id = ?";
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
    public List<Project> findAll() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT * FROM projects";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                projects.add(mapRow(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return projects;
    }

    @Override
    public void deleteById(UUID id) {
        String deleteMembersSql = "DELETE FROM project_members WHERE project_id = ?";
        String deleteProjectSql = "DELETE FROM projects WHERE id = ?";
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteMembersSql)) {
                ps.setObject(1, id);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(deleteProjectSql)) {
                ps.setObject(1, id);
                ps.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveMembers(Project project) throws SQLException {
        String deleteSql = "DELETE FROM project_members WHERE project_id = ?";
        String insertSql = "INSERT INTO project_members (project_id, user_id) VALUES (?,?)";
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(deleteSql)) {
                ps.setObject(1, project.getId());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                for (User member : project.getMembers()) {
                    ps.setObject(1, project.getId());
                    ps.setObject(2, member.getId());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        }
    }

    private Set<User> loadMembers(UUID projectId) throws SQLException {
        Set<User> members = new HashSet<>();
        String sql = "SELECT u.* FROM users u JOIN project_members pm ON u.id = pm.user_id WHERE pm.project_id = ?";
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, projectId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                members.add(userRepository.mapRowPublic(rs));
            }
        }
        return members;
    }

    private Project mapRow(ResultSet rs) throws SQLException {
        UUID ownerId = rs.getObject("owner_id", UUID.class);
        User owner = userRepository.findById(ownerId).orElseThrow();
        Project project = new Project(
            rs.getString("name"),
            rs.getString("description"),
            owner
        );
        project.setId(rs.getObject("id", UUID.class));
        project.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        UUID projectId = project.getId();
        project.setMembers(loadMembers(projectId));
        return project;
    }
}

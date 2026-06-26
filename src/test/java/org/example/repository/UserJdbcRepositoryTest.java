package org.example.repository;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.example.domain.Role;
import org.example.domain.User;
import org.example.repository.jdbc.UserJdbcRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserJdbcRepositoryTest {

    private UserJdbcRepository repository;

    @BeforeEach
    void setUp() throws Exception {
        // H2 en memoria con nombre único por test — base de datos limpia cada vez
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:testdb_" + UUID.randomUUID() + ";DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        config.setPassword("");
        DataSource dataSource = new HikariDataSource(config);

        // Ejecutar schema.sql para crear las tablas
        String sql = Files.readString(Path.of("src/main/resources/schema.sql"));
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        }

        repository = new UserJdbcRepository(dataSource);
    }

    @Test
    void save_and_findById_returns_same_user() {
        User user = new User("keeanu", "keeanu@gmail.com", "hash123", Role.MEMBER);
        repository.save(user);

        Optional<User> found = repository.findById(user.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("keeanu");
        assertThat(found.get().getEmail()).isEqualTo("keeanu@gmail.com");
        assertThat(found.get().getRole()).isEqualTo(Role.MEMBER);
    }

    @Test
    void findAll_returns_all_saved_users() {
        repository.save(new User("user1", "user1@gmail.com", "hash1", Role.MEMBER));
        repository.save(new User("user2", "user2@gmail.com", "hash2", Role.ADMIN));

        List<User> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    void deleteById_removes_user() {
        User user = new User("toborra", "toborra@gmail.com", "hash", Role.MEMBER);
        repository.save(user);

        repository.deleteById(user.getId());

        assertThat(repository.findById(user.getId())).isEmpty();
    }

    @Test
    void findByEmail_returns_correct_user() {
        User user = new User("emailuser", "unique@gmail.com", "hash", Role.MEMBER);
        repository.save(user);

        Optional<User> found = repository.findByEmail("unique@gmail.com");

        assertThat(found).isPresent();
        assertThat(found.get().getUsername()).isEqualTo("emailuser");
    }
}

package org.example.Config;


import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import javax.sql.DataSource;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public class DatabaseConfig {

    public static DataSource creaDataSource() {
        HikariConfig config =  new HikariConfig();

        String setJdbcUrl = "jdbc:h2:mem:taskhub;DB_CLOSE_DELAY=-1";
        String setUsername = "sa";
        String setPassword = "";

        config.setJdbcUrl(setJdbcUrl);
        config.setUsername(setUsername);
        config.setPassword(setPassword);
        HikariDataSource dataSource = new HikariDataSource(config);

        try {
            Connection connection =  dataSource.getConnection();
            String sql =  Files.readString(Path.of("src/main/resources/schema.sql"));

            Statement statement = connection.createStatement();
            statement.execute(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return dataSource;

    
         
    }
    
}

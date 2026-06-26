package org.example.repository.jdbc;

import javax.sql.DataSource;

import org.example.repository.Repository;

public abstract class JdbcRepository<T,ID> implements Repository<T,ID> {
    protected final DataSource dataSource;

    public JdbcRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
}

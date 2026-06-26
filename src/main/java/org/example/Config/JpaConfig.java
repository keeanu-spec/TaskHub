package org.example.Config;

import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

public class JpaConfig {

    private static EntityManagerFactory instance;

    public static EntityManagerFactory getEntityManagerFactory() {
        if (instance == null) {
            instance = Persistence.createEntityManagerFactory("taskhub");
        }
        return instance;
    }

    public static void close() {
        if (instance != null && instance.isOpen()) {
            instance.close();
        }
    }
}

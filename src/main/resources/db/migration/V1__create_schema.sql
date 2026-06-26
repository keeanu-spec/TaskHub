-- ============================================================
-- V1 — Esquema inicial TaskHub
-- Crea todas las tablas base del proyecto.
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id            UUID         NOT NULL PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(100) NOT NULL,
    role          VARCHAR(20)  NOT NULL,
    created_at    TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS projects (
    id          UUID         NOT NULL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500),
    owner_id    UUID         NOT NULL REFERENCES users(id),
    created_at  TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS project_members (
    project_id UUID NOT NULL REFERENCES projects(id),
    user_id    UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (project_id, user_id)
);

CREATE TABLE IF NOT EXISTS tasks (
    id           UUID         NOT NULL PRIMARY KEY,
    title        VARCHAR(100) NOT NULL,
    description  VARCHAR(500),
    status       VARCHAR(20)  NOT NULL,
    priority     VARCHAR(20)  NOT NULL,
    assignee_id  UUID         REFERENCES users(id),
    project_id   UUID         NOT NULL REFERENCES projects(id),
    due_date     DATE,
    created_at   TIMESTAMP    NOT NULL
);

CREATE TABLE IF NOT EXISTS task_notes (
    id         UUID          NOT NULL PRIMARY KEY,
    task_id    UUID          NOT NULL REFERENCES tasks(id),
    content    VARCHAR(1000) NOT NULL,
    created_at TIMESTAMP     NOT NULL
);

CREATE TABLE IF NOT EXISTS time_entries (
    id               UUID      NOT NULL PRIMARY KEY,
    task_id          UUID      NOT NULL REFERENCES tasks(id),
    started_at       TIMESTAMP NOT NULL,
    ended_at         TIMESTAMP,
    duration_minutes BIGINT
);

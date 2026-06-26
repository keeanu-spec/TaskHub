-- ============================================================
-- V2 — Filesystem virtual: carpeta de cada proyecto
-- Añade folder_path a projects con default "/" para no romper
-- datos existentes.
-- ============================================================

ALTER TABLE projects
    ADD COLUMN IF NOT EXISTS folder_path VARCHAR(500) NOT NULL DEFAULT '/';

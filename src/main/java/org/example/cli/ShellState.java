package org.example.cli;

import org.example.domain.Project;
import java.util.Optional;

/**
 * Estado de navegación del filesystem virtual.
 *
 * Niveles:
 *  - Raíz "/"          → dirPath="/", currentProject=null
 *  - Carpeta           → dirPath="/trabajo", currentProject=null
 *  - Proyecto en raíz  → dirPath="/", currentProject=Project
 *  - Proyecto en carpeta → dirPath="/trabajo", currentProject=Project
 */
public class ShellState {

    private String  dirPath        = "/";
    private Project currentProject = null;

    // ── navegación ────────────────────────────────────────────────────────────

    public void enterFolder(String path) {
        this.dirPath        = path;
        this.currentProject = null;
    }

    public void enterProject(Project p) {
        this.currentProject = p;
    }

    public void goUp() {
        if (currentProject != null) {
            // Salir del proyecto → quedarse en la carpeta
            currentProject = null;
        } else if (!dirPath.equals("/")) {
            // Salir de la carpeta → subir un nivel
            dirPath = VirtualFilesystem.parentOf(dirPath);
        }
    }

    public void goRoot() {
        dirPath        = "/";
        currentProject = null;
    }

    // ── estado ────────────────────────────────────────────────────────────────

    public boolean isRoot()          { return dirPath.equals("/") && currentProject == null; }
    public boolean isInProject()     { return currentProject != null; }
    public boolean isInFolder()      { return currentProject == null && !dirPath.equals("/"); }

    public String dirPath()          { return dirPath; }
    public Optional<Project> currentProject() { return Optional.ofNullable(currentProject); }

    // ── presentación ─────────────────────────────────────────────────────────

    public String path() {
        String p = dirPath.equals("/") ? "" : dirPath;
        return currentProject != null ? p + "/" + currentProject.getName() : (p.isEmpty() ? "/" : p);
    }

    public String prompt() {
        String C = "\u001B[36m", Y = "\u001B[33m", R = "\u001B[0m";
        String p = dirPath.equals("/") ? "~" : "~" + dirPath;
        if (currentProject != null) p += "/" + currentProject.getName();
        return C + "TaskHub" + R + " " + Y + p + R + "> ";
    }
}

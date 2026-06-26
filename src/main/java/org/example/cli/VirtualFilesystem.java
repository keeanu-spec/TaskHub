package org.example.cli;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * Filesystem virtual de carpetas.
 * Las carpetas no tienen entidad en BD — se persisten en data/folders.dat.
 * Los proyectos viven dentro de estas carpetas (folderPath en Project).
 */
public class VirtualFilesystem {

    private static final String DATA_FILE = "data/folders.dat";
    private final Set<String> folders = new LinkedHashSet<>();

    public VirtualFilesystem() {
        folders.add("/"); // raíz siempre existe
        load();
    }

    /** Crea una carpeta en la ruta dada (ej: "/trabajo/dev"). */
    public void mkdir(String path) {
        folders.add(normalize(path));
        save();
    }

    /** Devuelve los nombres de subcarpetas directas de un path. */
    public List<String> childFolders(String parentPath) {
        String parent = normalize(parentPath);
        List<String> children = new ArrayList<>();
        for (String f : folders) {
            if (f.equals("/")) continue;
            String parentOfF = parentOf(f);
            if (parentOfF.equals(parent)) {
                children.add(f.substring(f.lastIndexOf('/') + 1));
            }
        }
        return children;
    }

    public boolean exists(String path) {
        return folders.contains(normalize(path));
    }

    /** Construye la ruta hija: /trabajo + dev = /trabajo/dev */
    public static String join(String parent, String child) {
        String p = parent.endsWith("/") ? parent.substring(0, parent.length() - 1) : parent;
        return p.isEmpty() ? "/" + child : p + "/" + child;
    }

    /** Devuelve el padre de un path: /trabajo/dev -> /trabajo, /trabajo -> / */
    public static String parentOf(String path) {
        if (path == null || path.equals("/")) return "/";
        int idx = path.lastIndexOf('/');
        if (idx <= 0) return "/";
        return path.substring(0, idx);
    }

    private String normalize(String path) {
        if (path == null || path.isBlank()) return "/";
        path = path.replace("\\", "/");
        if (!path.startsWith("/")) path = "/" + path;
        while (path.endsWith("/") && path.length() > 1) path = path.substring(0, path.length() - 1);
        return path;
    }

    public void rmdir(String path) {
        folders.remove(normalize(path));
        save();
    }

    private void save() {
        try {
            Files.createDirectories(Paths.get("data"));
            Files.write(Paths.get(DATA_FILE), String.join("\n", folders).getBytes());
        } catch (IOException e) { /* ignora */ }
    }

    private void load() {
        try {
            File f = new File(DATA_FILE);
            if (!f.exists()) return;
            for (String line : Files.readAllLines(f.toPath())) {
                if (!line.isBlank()) folders.add(line.trim());
            }
        } catch (IOException e) { /* ignora */ }
    }
}

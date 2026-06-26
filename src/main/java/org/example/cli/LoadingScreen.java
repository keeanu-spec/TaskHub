package org.example.cli;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Pantalla de carga ANSI que se muestra mientras inicializa JPA.
 * Corre en un hilo separado y se detiene llamando a stop().
 */
public class LoadingScreen {

    private static final String RESET  = "[0m";
    private static final String CYAN   = "[36m";
    private static final String BOLD   = "[1m";
    private static final String YELLOW = "[33m";
    private static final String GREEN  = "[32m";
    private static final String CLEAR  = "[2J[H";   // limpiar pantalla
    private static final String UP     = "[%dA";           // subir N líneas
    private static final String ERASE  = "[2K";            // borrar línea

    private static final String[] BANNER = {
        "  / \\__         _  _____________  _   ___  __  ________    __",
        " (    @\\___     | |/ / ____/ ____|| | / / |/ / / ____/ /   /  |",
        " /         O    | ' /| |__ | |__  | |/ /|   / | /   | |   | | |",
        "/   (_____/     |  < |  __||  __| |    \\|    \\ | |   | |   | | |",
        "/_____/   U     |_|\\_\\|____||____||_|\\_\\|_|\\_\\ \\____/|_____|___/"
    };

    private static final String[] SPINNER = {"⠋","⠙","⠹","⠸","⠼","⠴","⠦","⠧","⠇","⠏"};
    private static final int BAR_WIDTH = 30;

    private final AtomicBoolean running = new AtomicBoolean(true);
    private volatile Thread thread;

    public void start() {
        thread = new Thread(() -> {
            System.out.print(CLEAR);
            printBanner();
            System.out.println();

            int step = 0;
            int progress = 0;
            while (running.get()) {
                progress = Math.min(progress + 1, BAR_WIDTH - 2);
                printLoadingBar(step, progress);
                step = (step + 1) % SPINNER.length;
                try { Thread.sleep(60); } catch (InterruptedException e) { break; }
                // Mover cursor hacia arriba para redibujar solo la barra
                System.out.print(String.format("[2A"));
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    /** Detiene la animación y limpia la pantalla. */
    public void stop() {
        running.set(false);
        if (thread != null) {
            try { thread.join(500); } catch (InterruptedException ignored) {}
        }
        System.out.print(CLEAR);
    }

    private void printBanner() {
        for (String line : BANNER) {
            System.out.println(BOLD + CYAN + line + RESET);
        }
    }

    private void printLoadingBar(int spinStep, int filled) {
        String spinner = YELLOW + BOLD + SPINNER[spinStep] + RESET;
        int percent = (filled * 100) / BAR_WIDTH;

        StringBuilder bar = new StringBuilder();
        bar.append(GREEN).append("[");
        for (int i = 0; i < BAR_WIDTH; i++) {
            bar.append(i < filled ? "█" : "░");
        }
        bar.append("]").append(RESET);

        System.out.println(ERASE + "  " + spinner + "  Iniciando TaskHub...  " + bar + "  " + BOLD + percent + "%" + RESET);
        System.out.println(ERASE + "  " + "[2m" + "Cargando base de datos..." + RESET);
    }
}

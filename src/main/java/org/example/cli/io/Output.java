package org.example.cli.io;

public class Output {
    private static final String GREEN  = "\u001B[32m";
    private static final String RED    = "\u001B[31m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE   = "\u001B[34m";
    private static final String CYAN   = "\u001B[36m";
    private static final String BOLD   = "\u001B[1m";
    private static final String RESET  = "\u001B[0m";

    public void success(String message) { System.out.println(GREEN  + message + RESET); }
    public void error(String message)   { System.out.println(RED    + message + RESET); }
    public void info(String message)    { System.out.println(BLUE   + message + RESET); }
    public void warning(String message) { System.out.println(YELLOW + message + RESET); }
    public void cyan(String message)    { System.out.println(CYAN   + message + RESET); }
    public void bold(String message)    { System.out.println(BOLD   + message + RESET); }
    public void print(String message)   { System.out.println(message); }
}

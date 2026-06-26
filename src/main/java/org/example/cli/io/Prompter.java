package org.example.cli.io;

import org.jline.reader.LineReader;


public class Prompter {

    private final LineReader lineReader;

    public Prompter(LineReader lineReader) {
        this.lineReader = lineReader;
    }
    

    public String prompt(String question){
        String input = lineReader.readLine(question);
        return input;
    }

    public String promptWithDefault(String question, String defaultValue){
        String input = lineReader.readLine(question);

        if(input.isBlank()) {
            return defaultValue;
        }
        return input;
    }

    public String promptPassword(String question){
       String input =  lineReader.readLine(question, '*');
        return input;
    }
}

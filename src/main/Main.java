package main;

import parser.PseudoParserDriver;
import parser.PseudoValidator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) throws IOException {
        String filePath = "ejemplo.txt";
        if (args.length > 0) filePath = args[0];

        Path p = Paths.get(filePath);
        if (!Files.exists(p)) {
            System.out.println("No encontré el archivo: " + filePath);
            System.out.println("Crea un archivo 'ejemplo.txt' en la raíz del proyecto con el pseudocódigo.");
            return;
        }

        String input = Files.readString(p);
        PseudoParserDriver.resetAll();
        PseudoValidator validator = new PseudoValidator();
        validator.parse(input);
        PseudoParserDriver.printReport();
    }
}

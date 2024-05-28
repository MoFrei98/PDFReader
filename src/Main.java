import gui.Reader;

import javax.swing.*;
import java.io.File;

public class Main {
    public static void main(String[] args) {
        String filePath = getPDFFilePathFromArgs(args);
        if (filePath == null)
            filePath = findFirstPDFInTestFilesFolder();

        if (filePath == null) {
            System.err.println("Keine PDF-Datei gefunden.");
            System.exit(1); // Programm mit Fehlercode beenden
        }

        File pdfFile = new File(filePath);
        SwingUtilities.invokeLater(() -> {
            Reader reader = new Reader(pdfFile);
            reader.setVisible(true);
        });

        /*
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                File pdfFile = new File(filePath);
                Reader reader = new Reader(pdfFile);
                reader.setVisible(true);
            }
        });
         */
    }

    private static String getPDFFilePathFromArgs(String[] args) {
        if (args.length > 0) {
            String filePath = args[0];
            File file = new File(filePath);
            if (file.exists() && file.isFile() && filePath.toLowerCase().endsWith(".pdf")) {
                return filePath;
            }
        }
        return null;
    }

    private static String findFirstPDFInTestFilesFolder() {
        File testFilesFolder = new File("files-to-read");
        if (testFilesFolder.isDirectory()) {
            File[] files = testFilesFolder.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                    return file.getAbsolutePath();
                }
            }
        }
        return null;
    }
}

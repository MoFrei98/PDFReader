import gui.Reader;

import javax.swing.*;
import java.io.File;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {
        File pdfFile = findFirstPDFInFilesFolder();
        if (pdfFile == null)
            System.out.println("No PDF found in files-to-read folder");

        SwingUtilities.invokeLater(() -> {
            try {
                Reader reader = new Reader(pdfFile);
                reader.setEnabled(true);
                reader.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Error in PDF-Reader: " + e.getMessage());
            }
        });

        /*
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Reader reader = new Reader(pdfFile);
                reader.setEnabled(true);
                reader.setVisible(true);
            }
        });
         */
    }

    private static File findFirstPDFInFilesFolder() {
        File pdfFilesFolder = new File("files-to-read");
        if (!pdfFilesFolder.exists())
            pdfFilesFolder.mkdir();

        if (pdfFilesFolder.isDirectory()) {
            for (File file : Objects.requireNonNull(pdfFilesFolder.listFiles())) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                    return file;
                }
            }
        }
        return null;
    }
}

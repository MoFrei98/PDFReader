package gui;

import javax.swing.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

public class Reader extends JFrame {

    private File pdf;
    private JPanel panel;

    public Reader(File pdf) {
        this.pdf = pdf;

        setTitle("PDF Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                displayPDF(g);
            }
        };
        getContentPane().add(panel);
    }

    public void setCurrentFile(String filePath) {
        try {
            Method m = getClass().getSuperclass().getDeclaredMethod("openPDFFile", new Class<?>[]{String.class, String.class});
            m.setAccessible(true);
            m.invoke(this, filePath, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayPDF(Graphics g) {
        try {
            PDDocument document = Loader.loadPDF(pdf);
            PDFRenderer renderer = new PDFRenderer(document);

            int numPages = document.getNumberOfPages();
            int dpi = 72; // Dots per Inch. Je höher, desto besser die Qualität.

            for (int i = 0; i < numPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            }

            document.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

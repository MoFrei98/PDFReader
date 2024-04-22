package gui;

import javax.swing.*;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Reader extends JFrame {

    private File pdf;
    private ArrayList<String> extractedText;

    private JTabbedPane tabbedPane;
    private JPanel pdfPanel;
    private JPanel extractedTextPanel;

    public Reader(File pdf) {
        this.pdf = pdf;

        setTitle("PDF Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { // Wenn der Tab "Extracted Text" ausgewählt wird
               extractTextClick();
            }
        });

        /* pdfPanel */
        pdfPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                try {
                    displayPDF(g);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(600, 800); // Vorgegebene Größe des pdfPanel
            }
        };

        JScrollPane pdfScrollPane = new JScrollPane(pdfPanel); // Einbetten des pdfPanel in ein JScrollPane
        tabbedPane.addTab("PDF", pdfScrollPane);

        /* extractdTextPanel */
        extractedTextPanel = new JPanel();
        extractedTextPanel.setLayout(new BorderLayout());
        JTextArea textArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(textArea);
        extractedTextPanel.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Extracted Text", extractedTextPanel);

        add(tabbedPane, BorderLayout.CENTER);

        /* bottomPanel */
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() / 9));
        add(bottomPanel, BorderLayout.SOUTH);

        JButton btnExtractText = new JButton("Extract Text");
        btnExtractText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extractTextClick();
            }
        });
        bottomPanel.add(btnExtractText);
    }

    private void displayPDF(Graphics g) throws IOException {
        PDDocument document = null;
        try {
            document = Loader.loadPDF(pdf);
            PDFRenderer renderer = new PDFRenderer(document);

            int numPages = document.getNumberOfPages();
            int dpi = 100; // Dots per Inch. Je höher, desto besser die Qualität.

            for (int i = 0; i < numPages; i++) {
                BufferedImage image = renderer.renderImageWithDPI(i, dpi);
                g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (document != null)
                document.close();
        }
    }

    private void extractTextClick() {
        try {
            extractedText = extractText();
            //textArea.setText(extractedText);
            tabbedPane.setSelectedIndex(1); // Wechseln zum Tab "Extracted Text"
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ArrayList<String> extractText() throws IOException {
        ArrayList<String> extractedText = new ArrayList<>();

        PDDocument document = null;
        try {
            document = Loader.loadPDF(pdf);

            for (int i = 0; i < document.getNumberOfPages(); i++) {
                extractedText.add(extractTextFromPage(document));
            }
            //extractedText.add(extractTextFromPage(document));

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (document != null)
                document.close();
        }
        return extractedText;
    }

    private String extractTextFromPage(PDDocument document) throws IOException {
        return new PDFTextStripper().getText(document);
    }
}

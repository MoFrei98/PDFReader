package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Reader extends JFrame {

    private File pdf;
    private ArrayList<String> extractedText;

    private JTabbedPane tabbedPane;
    private JPanel pdfPanel;
    private JPanel extractedTextPanel;
    private JTextArea textArea;
    private static final int FILTER_INDEX_TEXT = 0;
    private static final int FILTER_INDEX_DATE = 1;
    private static final int FILTER_INDEX_TIME = 2;
    private static final int FILTER_INDEX_PRICE = 3;
    private static final String[] FILTER_OPTIONS = { "Text", "Datum", "Uhrzeit", "Preis" };
    private JComboBox<String> filterDropdown;
    private JTextField filterTextField;
    private JCheckBox ignoreCaseCheckBox;
    private Highlighter highlighter = null;

    private boolean isExtracted = false;

    public Reader(File pdf) {
        this.pdf = pdf;

        setTitle("PDF Reader");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(950, 700);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { // Wenn der Tab "Extracted Text" ausgewählt wird
                extractTextOfPdf();
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
        extractedTextPanel = new JPanel(new BorderLayout());
        extractedTextPanel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_ENTER) {
                    filterTextField.requestFocusInWindow();
                }
            }
        });
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JLabel filterLabel = new JLabel("Filter:");
        filterDropdown = new JComboBox<>(FILTER_OPTIONS);
        filterDropdown.addActionListener(e -> {
            onFilterChange();
        });
        filterTextField = new JTextField(20);
        filterTextField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    filter(true);
            }

            @Override
            public void keyReleased(KeyEvent e) {}
        });
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                if (filterTextField.getText().length() >= 2)
                    filter(false);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                if (!filterTextField.getText().isEmpty() && filterTextField.getText().length() >= 2)
                    filter(false);
                else if (filterTextField.getText().isEmpty() && highlighter != null)
                    highlighter.removeAllHighlights();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });
        ignoreCaseCheckBox = new JCheckBox("IgnoreCase");
        ignoreCaseCheckBox.setSelected(true);
        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(e -> {
            filter(true);
        });
        filterPanel.add(filterLabel);
        filterPanel.add(filterDropdown);
        filterPanel.add(filterTextField);
        filterPanel.add(ignoreCaseCheckBox);
        filterPanel.add(filterButton);
        extractedTextPanel.add(filterPanel, BorderLayout.NORTH);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        extractedTextPanel.add(scrollPane, BorderLayout.CENTER);
        tabbedPane.addTab("Text", extractedTextPanel);
        add(tabbedPane, BorderLayout.CENTER);

        /* bottomPanel */
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottomPanel.setPreferredSize(new Dimension(this.getWidth(), this.getHeight() / 9));
        add(bottomPanel, BorderLayout.SOUTH);

        JButton btnSelectPdf = new JButton("Select PDF");
        btnSelectPdf.addActionListener(e -> {
            selectPdf();
        });
        bottomPanel.add(btnSelectPdf);

        /*
        JButton btnExtractText = new JButton("Extract Text");
        btnExtractText.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                extractTextClick();
            }
        });
        bottomPanel.add(btnExtractText);
        */
    }

    private void onFilterChange() {
        if (filterDropdown.getSelectedIndex() != FILTER_INDEX_TEXT) {
            filterTextField.setText("");
            filterTextField.setEnabled(false);
            ignoreCaseCheckBox.setEnabled(false);
        } else {
            filterTextField.setEnabled(true);
            ignoreCaseCheckBox.setEnabled(true);
            filterTextField.requestFocusInWindow();
        }

        if (highlighter != null)
            highlighter.removeAllHighlights();

        filter(false);
    }

    private void filter(boolean manually) {
        switch(filterDropdown.getSelectedIndex()) {
            case FILTER_INDEX_TEXT:
                filterText(manually);
                break;
            case FILTER_INDEX_DATE:
                filterDate(manually);
                break;
            case FILTER_INDEX_TIME:
                filterTime(manually);
                break;
            case FILTER_INDEX_PRICE:
                filterPrice(manually);
                break;
            default:
                throw new IllegalArgumentException("Invalid filter option selected");
        }
    }

    private void filterText(boolean manually) {
        final String filterText = (ignoreCaseCheckBox.isSelected() ? filterTextField.getText().toLowerCase() : filterTextField.getText());
        if (!filterText.isEmpty()) {
            highlighter = textArea.getHighlighter();
            highlighter.removeAllHighlights();
            DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

            String pdfText = (ignoreCaseCheckBox.isSelected() ? textArea.getText().toLowerCase() : textArea.getText());
            int index = 0;
            int found = 0;
            while ((index = pdfText.indexOf(filterText, index)) != -1) {
                found++;
                int endIndex = index + filterText.length();
                try {
                    highlighter.addHighlight(index, endIndex, highlightPainter);
                } catch (BadLocationException ex) {
                    ex.printStackTrace();
                }
                index = endIndex;
            }
            if (found == 0 && manually)
                JOptionPane.showMessageDialog(null, "String '" + filterTextField.getText() + "' not found");
            else if (found > 0 && manually)
                JOptionPane.showMessageDialog(null, "Found " + found + " occurrences of '" + filterTextField.getText() + "' in the PDFs text");

            System.out.println("Found " + found + " occurrences of '" + filterTextField.getText() + "' in the PDFs text");

        } else
            JOptionPane.showMessageDialog(null, "Please enter a valid filter text");
    }

    private void filterDate(boolean manually) {
        final String datePattern = "\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}\\b"; // Datumsmuster (z.B. 01/01/2022, 01.01.2022, 01-01-2022)
        int found = filterWithPattern(datePattern);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No dates found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " date occurrences in the PDFs text");
    }

    private void filterTime(boolean manually) {
        final String timePattern = "\\b\\d{1,2}:\\d{2}\\b"; // Zeitmuster (z.B. 12:00)
        int found = filterWithPattern(timePattern);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No times found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " time occurrences in the PDFs text");
    }

    private void filterPrice(boolean manually) {
        final String pricePattern = "\\b\\d+(\\.\\d{2})?\\s*[€$]\\b"; // Preis-Muster (z.B. 10.00 €, 10.00 $)
        int found = filterWithPattern(pricePattern);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No prices found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " price occurrences in the PDFs text");
    }

    private int filterWithPattern(String pattern) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(textArea.getText());

        highlighter = textArea.getHighlighter();
        highlighter.removeAllHighlights();
        DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

        int found = 0;
        while (m.find()) {
            found++;
            try {
                highlighter.addHighlight(m.start(), m.end(), highlightPainter);
            } catch (BadLocationException ex) {
                ex.printStackTrace();
            }
        }

        System.out.println("Found " + found + " occurrences of pattern '" + pattern + "' in the PDFs text");

        return found;
    }

    /**
     * Select a PDF file from the file system to load it into the GUI.
     */
    private void selectPdf() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select PDF File");
        fileChooser.setFileFilter(new FileNameExtensionFilter("PDF Files", "pdf"));

        int result = fileChooser.showOpenDialog(Reader.this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            this.pdf = selectedFile;
            try {
                Loader.loadPDF(this.pdf);
                this.isExtracted = false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            tabbedPane.setSelectedIndex(0);
            pdfPanel.repaint();
        }
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

    private void extractTextOfPdf() {
        try {
            if (!isExtracted) {
                extractedText = extractText();
                StringBuilder textToShow = new StringBuilder();
                for (String line : extractedText)
                    textToShow.append(line).append("\n");
                textArea.setText(textToShow.toString());
                textArea.setCaretPosition(0);
            }

            tabbedPane.setSelectedIndex(1); // Wechseln zum Tab "Extracted Text"
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            isExtracted = true;
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

            System.out.println("extracted the following text from PDF " + pdf.getAbsolutePath() + ": ");
            for (String line : extractedText)
                System.out.println(line);
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

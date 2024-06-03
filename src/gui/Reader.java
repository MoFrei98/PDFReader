package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import utils.Filter;

import java.awt.*;
import java.awt.event.*;
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
    private JTextArea textArea;
    private JComboBox<String> filterDropdown;
    private static final int FILTER_INDEX_TEXT = 0;
    private static final int FILTER_INDEX_DATE = 1;
    private static final int FILTER_INDEX_TIME = 2;
    private static final int FILTER_INDEX_PRICE = 3;
    private static final String[] FILTER_OPTIONS = { "Text", "Date", "Time", "Price" };
    private JTextField filterTextField;
    private JCheckBox ignoreCaseCheckBox;
    private JCheckBox wholeWordCheckBox;

    private boolean isExtracted = false;

    public Reader(File pdf) {
        this.pdf = pdf;

        if (this.pdf != null)
            System.out.println("Loading PDF: " + this.pdf.getAbsolutePath());

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
                    if (pdf != null)
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
                else if (filterTextField.getText().isEmpty())
                    textArea.getHighlighter().removeAllHighlights();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {}
        });

        ignoreCaseCheckBox = new JCheckBox("Ignore Case");
        ignoreCaseCheckBox.addActionListener(e -> {
            filter(false);
        });
        ignoreCaseCheckBox.setSelected(true);

        wholeWordCheckBox = new JCheckBox("Whole Word");
        wholeWordCheckBox.addActionListener(e -> {
            filter(false);
        });
        wholeWordCheckBox.setSelected(false);

        JButton filterButton = new JButton("Filter");
        filterButton.addActionListener(e -> {
            filter(true);
        });

        filterPanel.add(filterLabel);
        filterPanel.add(filterDropdown);
        filterPanel.add(filterTextField);
        filterPanel.add(ignoreCaseCheckBox);
        filterPanel.add(wholeWordCheckBox);
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

        textArea.getHighlighter().removeAllHighlights();

        filter(false);
    }

    private void filter(boolean manually) {
        switch(filterDropdown.getSelectedIndex()) {
            case FILTER_INDEX_TEXT:
                String filterText = (ignoreCaseCheckBox.isSelected() ? filterTextField.getText().toLowerCase() : filterTextField.getText());
                if (wholeWordCheckBox.isSelected())
                    filterText = " " + filterText + " ";
                final String pdfText = (ignoreCaseCheckBox.isSelected() ? textArea.getText().toLowerCase() : textArea.getText());
                Filter.text(textArea, pdfText, filterText, manually);
                break;
            case FILTER_INDEX_DATE:
                Filter.date(textArea, manually);
                break;
            case FILTER_INDEX_TIME:
                Filter.time(textArea, manually);
                break;
            case FILTER_INDEX_PRICE:
                Filter.price(textArea, manually);
                break;
            default:
                throw new IllegalArgumentException("Invalid filter option selected");
        }
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
            this.pdf = fileChooser.getSelectedFile();;
            System.out.println("Loading PDF: " + pdf.getAbsolutePath());
            /*
            try {
                Loader.loadPDF(this.pdf);
                this.isExtracted = false;
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            */
            this.isExtracted = false;
            tabbedPane.setSelectedIndex(0);
            pdfPanel.repaint();
        }
    }

    private void displayPDF(Graphics g) throws IOException {
        PDDocument document = null;
        try {
            document = Loader.loadPDF(this.pdf);
            PDFRenderer renderer = new PDFRenderer(document);

            int numPages = document.getNumberOfPages();
            final int dpi = 100;

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
            document = Loader.loadPDF(this.pdf);
            for (int i = 0; i < document.getNumberOfPages(); i++)
                extractedText.add(new PDFTextStripper().getText(document));

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
}

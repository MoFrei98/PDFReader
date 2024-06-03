package utils;

import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Filter {
    private static final String DATE_PATTERN = "\\b\\d{1,2}[./-]\\d{1,2}[./-]\\d{4}\\b"; // Datumsmuster (z.B. 01/01/2022, 01.01.2022, 01-01-2022)
    private static final String TIME_PATTERN = "\\b\\d{1,2}:\\d{2}\\b"; // Zeitmuster (z.B. 12:00)
    private static final String PRICE_PATTERN = "\\b\\d+([.,]\\d{2})?\\s*[€$]"; // Preis-Muster (z.B. 10.00 €, 10,00 €, 10.00$, 10 $)

    /**
     * Filters the TextArea for the given text
     * @param textArea
     * @param text
     * @param filterText
     * @param manually
     */
    public static void text(JTextArea textArea, final String text, final String filterText, boolean manually) {
        if (!filterText.isEmpty()) {
            Highlighter highlighter = textArea.getHighlighter();
            highlighter.removeAllHighlights();
            DefaultHighlighter.DefaultHighlightPainter highlightPainter = new DefaultHighlighter.DefaultHighlightPainter(Color.YELLOW);

            int index = 0;
            int found = 0;
            while ((index = text.indexOf(filterText, index)) != -1) {
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
                JOptionPane.showMessageDialog(null, "String '" + filterText + "' not found");
            else if (found > 0 && manually)
                JOptionPane.showMessageDialog(null, "Found " + found + " occurrences of '" + filterText + "' in the PDFs text");

            System.out.println("Found " + found + " occurrences of '" + filterText + "' in the PDFs text");

        } else if (manually)
            JOptionPane.showMessageDialog(null, "Please enter a valid filter text");
    }

    /**
     * Filters the TextArea for dates
     * @param textArea
     * @param manually
     */
    public static void date(JTextArea textArea, boolean manually) {
        int found = filterWithPattern(textArea, DATE_PATTERN);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No dates found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " date occurrences in the PDFs text");
    }

    /**
     * Filtes the TextArea for times
     * @param textArea
     * @param manually
     */
    public static void time(JTextArea textArea, boolean manually) {
        int found = filterWithPattern(textArea, TIME_PATTERN);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No times found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " time occurrences in the PDFs text");
    }

    /**
     * Filters the TextArea for prices
     * @param textArea
     * @param manually
     */
    public static void price(JTextArea textArea, boolean manually) {
        int found = filterWithPattern(textArea, PRICE_PATTERN);
        if (found == 0 && manually)
            JOptionPane.showMessageDialog(null, "No prices found in the PDFs text");
        else if (found > 0 && manually)
            JOptionPane.showMessageDialog(null, "Found " + found + " price occurrences in the PDFs text");
    }

    /**
     * Filters the TextArea with the given pattern
     * @param textArea
     * @param pattern
     * @return
     */
    private static int filterWithPattern(JTextArea textArea, final String pattern) {
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(textArea.getText());

        Highlighter highlighter = textArea.getHighlighter();
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
}

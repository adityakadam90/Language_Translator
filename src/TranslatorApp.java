import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TranslatorApp extends JFrame {
    private static final Map<String, String> languageCodes = new HashMap<>();

    static {
        // Initialize language codes
        languageCodes.put("auto", "Automatic");
        languageCodes.put("af", "Afrikaans");
        languageCodes.put("sq", "Albanian");
        languageCodes.put("ar", "Arabic");
        languageCodes.put("en", "English");
        languageCodes.put("hi", "Hindi");
        languageCodes.put("mr", "Marathi");
        languageCodes.put("fr", "French");
        languageCodes.put("ja", "Japanese"); // Japanese language code
        languageCodes.put("zh", "Chinese (Simplified)"); // Chinese (Simplified) language code
        languageCodes.put("es", "Spanish");
        languageCodes.put("de", "German");
        languageCodes.put("it", "Italian");
    }

    private JComboBox<String> sourceLanguageCombo;
    private JComboBox<String> targetLanguageCombo;
    private JTextArea textInputArea;
    private JTextArea textOutputArea;

    public TranslatorApp() {
        super("Language Translator");

        // Initialize components
        sourceLanguageCombo = new JComboBox<>(languageCodes.values().toArray(new String[0]));
        targetLanguageCombo = new JComboBox<>(languageCodes.values().toArray(new String[0]));
        textInputArea = new JTextArea(10, 30);
        textOutputArea = new JTextArea(10, 30);
        textOutputArea.setEditable(false);
        Font unicodeFont = new Font("Arial Unicode MS", Font.PLAIN, 12); // Example font
        textOutputArea.setFont(unicodeFont);

        JButton translateButton = new JButton("Translate");
        translateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                translateText();
            }
        });

        JButton searchLanguageButton = new JButton("Search Language");
        searchLanguageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                searchLanguage();
            }
        });

        // Create panels for layout
        JPanel inputPanel = new JPanel(new BorderLayout(10, 10));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel languagePanel = new JPanel(new GridLayout(2, 2, 10, 10));
        languagePanel.add(new JLabel("Source Language:"));
        languagePanel.add(sourceLanguageCombo);
        languagePanel.add(new JLabel("Target Language:"));
        languagePanel.add(targetLanguageCombo);

        inputPanel.add(languagePanel, BorderLayout.NORTH);
        inputPanel.add(new JScrollPane(textInputArea), BorderLayout.CENTER);
        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        buttonPanel.add(translateButton);
        buttonPanel.add(searchLanguageButton);
        inputPanel.add(buttonPanel, BorderLayout.SOUTH);

        JPanel outputPanel = new JPanel(new BorderLayout(10, 10));
        outputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        outputPanel.add(new JLabel("Translated Text:"), BorderLayout.NORTH);
        outputPanel.add(new JScrollPane(textOutputArea), BorderLayout.CENTER);

        // Add panels to the frame
        Container contentPane = getContentPane();
        contentPane.setLayout(new GridLayout(2, 1));
        contentPane.add(inputPanel);
        contentPane.add(outputPanel);

        // Set frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack(); // Adjusts frame size based on components
        setLocationRelativeTo(null); // Center the frame
        setVisible(true);
    }

    private void translateText() {
        String text = textInputArea.getText();
        String sourceLang = getKeyFromValue(languageCodes, (String) sourceLanguageCombo.getSelectedItem());
        String targetLang = getKeyFromValue(languageCodes, (String) targetLanguageCombo.getSelectedItem());

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://google-translate113.p.rapidapi.com/api/v1/translator/text"))
                    .header("content-type", "application/x-www-form-urlencoded")
                    .header("X-RapidAPI-Key", "6733e02147msh4536cd67418a1abp13fe5ejsnd3e4c25e0b40")
                    .header("X-RapidAPI-Host", "google-translate113.p.rapidapi.com")
                    .POST(HttpRequest.BodyPublishers.ofString("from=" + sourceLang + "&to=" + targetLang + "&text=" + text))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient().send(request,
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

            String responseBody = response.body();
            int startIndex = responseBody.indexOf("\"trans\":\"") + "\"trans\":\"".length();
            int endIndex = responseBody.indexOf("\"", startIndex);
            String translatedText = responseBody.substring(startIndex, endIndex);
            textOutputArea.setText(translatedText);
        } catch (Exception e) {
            textOutputArea.setText("Translation failed: " + e.getMessage());
        }
    }

    private String getKeyFromValue(Map<String, String> map, String value) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                return entry.getKey();
            }
        }
        return null; // Should not occur if the value exists in the map
    }

    private void searchLanguage() {
        String searchTerm = JOptionPane.showInputDialog(this, "Enter language name to search:");

        if (searchTerm != null && !searchTerm.isEmpty()) {
            String result = languageCodes.entrySet().stream()
                    .filter(entry -> entry.getValue().toLowerCase().contains(searchTerm.toLowerCase()))
                    .map(Map.Entry::getValue)
                    .collect(Collectors.joining(", "));

            if (!result.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Languages found: " + result);
            } else {
                JOptionPane.showMessageDialog(this, "No languages found matching '" + searchTerm + "'");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new TranslatorApp());
    }
}

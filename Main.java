
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.util.*;

public class Main extends JFrame {

    static final String USERS = "users.csv";
    static final String SURVEYS = "surveys.csv";
    static final String RESPONSES = "responses.csv";

    String currentUser;

    JTextField usernameField, titleField, questionField;
    JPasswordField passwordField;

    JTextArea answerArea, questionDisplay;

    JComboBox<String> surveyBox;

    DefaultTableModel surveyModel, responseModel;

    HashMap<String, String> surveyIdMap = new HashMap<>();

    public Main() {

        setTitle("Online Survey System");
        setSize(850, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loginPage();
    }

    // Initialize CSV Files
    void initializeFiles() {

        try {

            for (String file : new String[]{
                    USERS, SURVEYS, RESPONSES}) {

                File f = new File(file);

                if (!f.exists()) {
                    f.createNewFile();
                }
            }

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Save Data
    void saveData(String file, String data) {

        try (FileWriter fw = new FileWriter(file, true)) {

            fw.write(data + "\n");

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // Read Data
    ArrayList<String[]> readData(String file) {

        ArrayList<String[]> records = new ArrayList<>();

        try (BufferedReader br =
                     new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                records.add(parseCSV(line));
            }

        } catch (IOException e) {

            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return records;
    }

    // Convert Data into CSV Format
    String csv(String value) {

        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    // Parse CSV Data
    String[] parseCSV(String line) {

        ArrayList<String> values = new ArrayList<>();

        StringBuilder value = new StringBuilder();

        boolean quoted = false;

        for (int i = 0; i < line.length(); i++) {

            char ch = line.charAt(i);

            if (ch == '"') {

                if (quoted && i + 1 < line.length()
                        && line.charAt(i + 1) == '"') {

                    value.append('"');
                    i++;

                } else {

                    quoted = !quoted;
                }

            } else if (ch == ',' && !quoted) {

                values.add(value.toString());

                value.setLength(0);

            } else {

                value.append(ch);
            }
        }

        values.add(value.toString());

        return values.toArray(new String[0]);
    }

    // Login Page
    void loginPage() {

        JPanel panel = new JPanel(
                new GridLayout(4, 2, 10, 10));

        panel.setBorder(BorderFactory.createEmptyBorder(
                80, 150, 80, 150));

        usernameField = new JTextField();

        passwordField = new JPasswordField();

        JButton login = new JButton("Login");

        JButton register = new JButton("Register");

        panel.add(new JLabel("ONLINE SURVEY SYSTEM"));

        panel.add(new JLabel(""));

        panel.add(new JLabel("Username:"));

        panel.add(usernameField);

        panel.add(new JLabel("Password:"));

        panel.add(passwordField);

        panel.add(login);

        panel.add(register);

        login.addActionListener(e -> loginUser());

        register.addActionListener(e -> registerUser());

        setContentPane(panel);

        revalidate();

        repaint();
    }

    // User Registration
    void registerUser() {

        String user = usernameField.getText().trim();

        String pass = new String(
                passwordField.getPassword());

        if (user.isEmpty() || pass.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please enter all details!");

            return;
        }

        for (String[] row : readData(USERS)) {

            if (row.length >= 2 &&
                    row[0].equals(user)) {

                JOptionPane.showMessageDialog(this,
                        "Username already exists!");

                return;
            }
        }

        saveData(USERS,
                csv(user) + "," + csv(pass));

        JOptionPane.showMessageDialog(this,
                "Registration Successful!");

        passwordField.setText("");
    }

    // User Login
    void loginUser() {

        String user = usernameField.getText().trim();

        String pass = new String(
                passwordField.getPassword());

        for (String[] row : readData(USERS)) {

            if (row.length >= 2 &&
                    row[0].equals(user) &&
                    row[1].equals(pass)) {

                currentUser = user;

                JOptionPane.showMessageDialog(this,
                        "Login Successful!");

                dashboard();

                return;
            }
        }

        JOptionPane.showMessageDialog(this,
                "Invalid Username or Password!");
    }

    // Dashboard
    void dashboard() {

        JTabbedPane tabs = new JTabbedPane();

        tabs.addTab("Create Survey", createSurveyPanel());

        tabs.addTab("View Surveys", viewSurveyPanel());

        tabs.addTab("Submit Response", responsePanel());

        tabs.addTab("View Responses", viewResponsesPanel());

        JButton logout = new JButton("Logout");

        logout.addActionListener(e -> loginPage());

        JPanel main = new JPanel(new BorderLayout());

        JLabel welcome = new JLabel(
                "Welcome, " + currentUser,
                JLabel.CENTER);

        welcome.setFont(new Font(
                "Arial", Font.BOLD, 15));

        main.add(welcome, BorderLayout.NORTH);

        main.add(tabs, BorderLayout.CENTER);

        main.add(logout, BorderLayout.SOUTH);

        setContentPane(main);

        revalidate();

        repaint();
    }

    // Create Survey
    JPanel createSurveyPanel() {

        JPanel panel = new JPanel(
                new GridLayout(5, 1, 10, 10));

        panel.setBorder(BorderFactory.createEmptyBorder(
                50, 100, 50, 100));

        titleField = new JTextField();

        questionField = new JTextField();

        JButton create = new JButton("Create Survey");

        panel.add(new JLabel("Survey Title:"));

        panel.add(titleField);

        panel.add(new JLabel("Survey Question:"));

        panel.add(questionField);

        panel.add(create);

        create.addActionListener(e -> {

            String title = titleField.getText().trim();

            String question = questionField.getText().trim();

            if (title.isEmpty() || question.isEmpty()) {

                JOptionPane.showMessageDialog(this,
                        "Please fill all fields!");

                return;
            }

            String id = UUID.randomUUID().toString();

            saveData(SURVEYS,
                    csv(id) + "," +
                    csv(title) + "," +
                    csv(question) + "," +
                    csv(currentUser));

            JOptionPane.showMessageDialog(this,
                    "Survey Created Successfully!");

            titleField.setText("");

            questionField.setText("");

            loadSurveys();

            loadSurveyOptions();
        });

        return panel;
    }

    // View Surveys
    JPanel viewSurveyPanel() {

        JPanel panel = new JPanel(new BorderLayout());

        surveyModel = new DefaultTableModel(
                new String[]{
                        "Survey ID",
                        "Title",
                        "Question",
                        "Creator"
                }, 0);

        JTable table = new JTable(surveyModel);

        table.setRowHeight(25);

        JButton refresh = new JButton("Refresh");

        refresh.addActionListener(e -> loadSurveys());

        panel.add(new JScrollPane(table),
                BorderLayout.CENTER);

        panel.add(refresh, BorderLayout.SOUTH);

        loadSurveys();

        return panel;
    }

    // Load Surveys
    void loadSurveys() {

        if (surveyModel == null) return;

        surveyModel.setRowCount(0);

        for (String[] row : readData(SURVEYS)) {

            if (row.length >= 4) {

                surveyModel.addRow(row);
            }
        }
    }

    // Submit Response Panel
    JPanel responsePanel() {

        JPanel panel = new JPanel(
                new BorderLayout(15, 15));

        panel.setBorder(BorderFactory.createEmptyBorder(
                25, 40, 25, 40));

        // Survey Selection
        JPanel top = new JPanel(
                new BorderLayout(5, 8));

        JLabel surveyLabel = new JLabel(
                "SELECT A SURVEY:");

        surveyLabel.setFont(new Font(
                "Arial", Font.BOLD, 15));

        surveyBox = new JComboBox<>();

        surveyBox.setFont(new Font(
                "Arial", Font.PLAIN, 14));

        surveyBox.setMaximumRowCount(5);

        top.add(surveyLabel, BorderLayout.NORTH);

        top.add(surveyBox, BorderLayout.CENTER);

        // Question Display
        JPanel questionPanel = new JPanel(
                new BorderLayout(5, 8));

        JLabel questionLabel = new JLabel(
                "SURVEY QUESTION:");

        questionLabel.setFont(new Font(
                "Arial", Font.BOLD, 15));

        questionDisplay = new JTextArea(3, 30);

        questionDisplay.setEditable(false);

        questionDisplay.setLineWrap(true);

        questionDisplay.setWrapStyleWord(true);

        questionDisplay.setFont(new Font(
                "Arial", Font.PLAIN, 14));

        questionDisplay.setMargin(
                new Insets(10, 10, 10, 10));

        questionDisplay.setBackground(
                new Color(245, 245, 245));

        JScrollPane questionScroll = new JScrollPane(
                questionDisplay);

        questionPanel.add(questionLabel,
                BorderLayout.NORTH);

        questionPanel.add(questionScroll,
                BorderLayout.CENTER);

        // Answer Section
        JPanel answerPanel = new JPanel(
                new BorderLayout(5, 8));

        JLabel answerLabel = new JLabel(
                "ENTER YOUR RESPONSE:");

        answerLabel.setFont(new Font(
                "Arial", Font.BOLD, 15));

        answerArea = new JTextArea(6, 30);

        answerArea.setLineWrap(true);

        answerArea.setWrapStyleWord(true);

        answerArea.setFont(new Font(
                "Arial", Font.PLAIN, 14));

        answerArea.setMargin(
                new Insets(10, 10, 10, 10));

        answerPanel.add(answerLabel,
                BorderLayout.NORTH);

        answerPanel.add(new JScrollPane(answerArea),
                BorderLayout.CENTER);

        // Center Layout
        JPanel center = new JPanel(
                new GridLayout(2, 1, 10, 10));

        center.add(questionPanel);

        center.add(answerPanel);

        // Buttons
        JButton refresh = new JButton("Refresh Surveys");

        JButton submit = new JButton("Submit Response");

        JPanel bottom = new JPanel(
                new FlowLayout(FlowLayout.RIGHT, 10, 5));

        bottom.add(refresh);

        bottom.add(submit);

        // Main Layout
        panel.add(top, BorderLayout.NORTH);

        panel.add(center, BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);

        refresh.addActionListener(e -> loadSurveyOptions());

        surveyBox.addActionListener(
                e -> displaySelectedQuestion());

        submit.addActionListener(e -> submitResponse());

        loadSurveyOptions();

        return panel;
    }

    // Load Survey Titles into Dropdown
    void loadSurveyOptions() {

        if (surveyBox == null) return;

        surveyBox.removeAllItems();

        surveyIdMap.clear();

        for (String[] row : readData(SURVEYS)) {

            if (row.length >= 4) {

                String shortId = row[0].substring(
                        0, Math.min(8, row[0].length()));

                String display = row[1] +
                        " (ID: " + shortId + ")";

                surveyBox.addItem(display);

                surveyIdMap.put(display, row[0]);
            }
        }

        surveyBox.setSelectedIndex(-1);

        if (questionDisplay != null) {
            questionDisplay.setText("");
        }
    }

    // Display Selected Survey Question
    void displaySelectedQuestion() {

        if (questionDisplay == null) return;

        String selected =
                (String) surveyBox.getSelectedItem();

        if (selected == null) {

            questionDisplay.setText("");

            return;
        }

        String surveyId = surveyIdMap.get(selected);

        for (String[] row : readData(SURVEYS)) {

            if (row.length >= 4 &&
                    row[0].equals(surveyId)) {

                questionDisplay.setText(row[2]);

                return;
            }
        }

        questionDisplay.setText("Question not found.");
    }

    // Submit Response
    void submitResponse() {

        String selected =
                (String) surveyBox.getSelectedItem();

        String answer =
                answerArea.getText().trim();

        if (selected == null || answer.isEmpty()) {

            JOptionPane.showMessageDialog(this,
                    "Please select a survey and enter your response!");

            return;
        }

        String surveyId = surveyIdMap.get(selected);

        if (surveyId == null) {

            JOptionPane.showMessageDialog(this,
                    "Invalid survey selection!");

            return;
        }

        saveData(RESPONSES,
                csv(surveyId) + "," +
                csv(currentUser) + "," +
                csv(answer));

        JOptionPane.showMessageDialog(this,
                "Response Submitted Successfully!");

        answerArea.setText("");

        surveyBox.setSelectedIndex(-1);
    }

    // View Submitted Responses
    JPanel viewResponsesPanel() {

        JPanel panel = new JPanel(
                new BorderLayout());

        responseModel = new DefaultTableModel(
                new String[]{
                        "Survey ID",
                        "Survey Question",
                        "Username",
                        "Response"
                }, 0);

        JTable table = new JTable(responseModel);

        table.setRowHeight(25);

        JButton refresh = new JButton(
                "Refresh Responses");

        refresh.addActionListener(e -> loadResponses());

        panel.add(new JScrollPane(table),
                BorderLayout.CENTER);

        panel.add(refresh, BorderLayout.SOUTH);

        loadResponses();

        return panel;
    }

    // Load Questions and Responses
    void loadResponses() {

        if (responseModel == null) return;

        responseModel.setRowCount(0);

        ArrayList<String[]> surveys =
                readData(SURVEYS);

        ArrayList<String[]> responses =
                readData(RESPONSES);

        for (String[] response : responses) {

            if (response.length >= 3) {

                String surveyId = response[0];

                String username = response[1];

                String answer = response[2];

                String question = "Question not found";

                for (String[] survey : surveys) {

                    if (survey.length >= 4 &&
                            survey[0].equals(surveyId)) {

                        question = survey[2];

                        break;
                    }
                }

                responseModel.addRow(new Object[]{
                        surveyId,
                        question,
                        username,
                        answer
                });
            }
        }
    }

    // Main Method
    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {

            Main app = new Main();

            app.initializeFiles();

            app.setVisible(true);
        });
    }
}

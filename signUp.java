import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class signUp extends JFrame {

    private JTextField email;
    private JTextField username;
    private JPasswordField password;
    private JPasswordField confirmPassword;
    private JProgressBar passwordStrengthBar;
    private JLabel strengthLabel;
    private JButton signInButton;
    private JButton backButton;
    private mainMenu mainMenu;

    public void setMainMenu(mainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public signUp() {
        setTitle("Sign Up");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(6, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField();

        JLabel usernameLabel = new JLabel("Username:");
        username = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        password = new JPasswordField();

        passwordStrengthBar = new JProgressBar(0, 4);
        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setStringPainted(false);
        passwordStrengthBar.setForeground(Color.RED);
        strengthLabel = new JLabel("Strength: Weak");

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPassword = new JPasswordField();

        signInButton = new JButton("Sign Up");
        backButton = new JButton("Back");

        panel.add(emailLabel);
        panel.add(email);
        panel.add(usernameLabel);
        panel.add(username);
        panel.add(passwordLabel);
        panel.add(password);
        panel.add(new JLabel("Password Strength:"));
        panel.add(passwordStrengthBar);
        panel.add(new JLabel(""));
        panel.add(strengthLabel);
        panel.add(confirmPasswordLabel);
        panel.add(confirmPassword);

        JPanel bottomPanel = new JPanel(new FlowLayout());
        bottomPanel.add(backButton);
        bottomPanel.add(signInButton);

        add(panel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        password.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                updateStrength();
            }

            public void removeUpdate(DocumentEvent e) {
                updateStrength();
            }

            public void changedUpdate(DocumentEvent e) {
                updateStrength();
            }
        });

        signInButton.addActionListener(e -> {
            String emailText = email.getText().trim();
            String usernameText = username.getText().trim();
            String passwordText = new String(password.getPassword());
            String confirmPasswordText = new String(confirmPassword.getPassword());

            if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(emailText)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!passwordText.equals(confirmPasswordText)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (FirebaseConnection.usernameExists(usernameText)) {
                JOptionPane.showMessageDialog(this, "Username already taken.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String hashedPassword = hashPassword(passwordText);
            boolean success = FirebaseConnection.sendData(emailText, usernameText, hashedPassword);

            if (success) {
                JOptionPane.showMessageDialog(this, "Sign Up Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                new login().setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send data. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        backButton.addActionListener(e -> {
            if (mainMenu != null) {
                mainMenu.setVisible(true);
            }
            dispose();
        });

        setVisible(true);
    }

    private void updateStrength() {
        String pwd = new String(password.getPassword());
        int score = getStrengthScore(pwd);

        passwordStrengthBar.setValue(score);

        switch (score) {
            case 0, 1 -> {
                passwordStrengthBar.setForeground(Color.RED);
                strengthLabel.setText("Strength: Weak");
            }
            case 2, 3 -> {
                passwordStrengthBar.setForeground(Color.ORANGE);
                strengthLabel.setText("Strength: Moderate");
            }
            case 4 -> {
                passwordStrengthBar.setForeground(Color.GREEN);
                strengthLabel.setText("Strength: Strong");
            }
        }
    }

    private int getStrengthScore(String pwd) {
        int score = 0;
        if (pwd.length() >= 8) score++;
        if (pwd.matches(".*[A-Z].*") && pwd.matches(".*[a-z].*")) score++;
        if (pwd.matches(".*[0-9].*")) score++;
        if (pwd.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) score++;
        return score;
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[\\w.-]+@[\\w.-]+\\.[A-Za-z]{2,}$";
        return Pattern.matches(emailRegex, email);
    }

    private String hashPassword(String password) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

}

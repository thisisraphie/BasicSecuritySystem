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
    private JPasswordField adminKeyField;
    private JCheckBox adminCheckBox;
    private JProgressBar passwordStrengthBar;
    private JLabel strengthLabel;
    private JButton signUpButton;
    private JButton backButton;
    private mainMenu mainMenu;
    private Timer strengthUpdateTimer;
    
    // Ito ang secret key para sa admin registration
    private static final String ADMIN_SECRET_KEY = "Admin123";

    public void setMainMenu(mainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public signUp() {
        setTitle("Sign Up");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 25);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 22);

        email = new JTextField(20);
        username = new JTextField(20);
        password = new JPasswordField(20);
        confirmPassword = new JPasswordField(20);
        adminKeyField = new JPasswordField(20);
        adminCheckBox = new JCheckBox("Register as Admin");
        passwordStrengthBar = new JProgressBar(0, 4);
        strengthLabel = new JLabel("Strength: Weak");
        signUpButton = new JButton();
        backButton = new JButton();

        JPanel bgPanel = new JPanel(new BorderLayout());
        bgPanel.setBackground(Color.LIGHT_GRAY);
        setContentPane(bgPanel);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        bgPanel.add(mainPanel, BorderLayout.CENTER);

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(3, 52, 110));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.dispose();
            }
        };
        formPanel.setOpaque(false);
        formPanel.setPreferredSize(new Dimension(700, 800));
        formPanel.setBorder(BorderFactory.createEmptyBorder(50, 70, 50, 70));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JLabel signUpLabel = new JLabel("Sign Up", SwingConstants.CENTER);
        signUpLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        signUpLabel.setForeground(Color.WHITE);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(signUpLabel, gbc);

        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;

        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(labelFont);
        emailLabel.setForeground(Color.WHITE);
        email.setFont(fieldFont);
        email.setPreferredSize(new Dimension(450, 40));
        gbc.gridy = 1;
        gbc.gridx = 0;
        formPanel.add(emailLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(email, gbc);

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setFont(labelFont);
        usernameLabel.setForeground(Color.WHITE);
        username.setFont(fieldFont);
        username.setPreferredSize(new Dimension(450, 40));
        gbc.gridy = 2;
        gbc.gridx = 0;
        formPanel.add(usernameLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(username, gbc);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setFont(labelFont);
        passwordLabel.setForeground(Color.WHITE);
        password.setFont(fieldFont);
        password.setPreferredSize(new Dimension(450, 40));
        gbc.gridy = 3;
        gbc.gridx = 0;
        formPanel.add(passwordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(password, gbc);

        passwordStrengthBar.setValue(0);
        passwordStrengthBar.setStringPainted(false);
        passwordStrengthBar.setPreferredSize(new Dimension(450, 20));
        passwordStrengthBar.setForeground(Color.PINK);

        strengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
        strengthLabel.setForeground(Color.WHITE);

        gbc.gridy = 4;
        gbc.gridx = 1;
        formPanel.add(passwordStrengthBar, gbc);

        gbc.gridy = 5;
        gbc.gridx = 1;
        formPanel.add(strengthLabel, gbc);

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setFont(labelFont);
        confirmPasswordLabel.setForeground(Color.WHITE);
        confirmPassword.setFont(fieldFont);
        confirmPassword.setPreferredSize(new Dimension(450, 40));
        gbc.gridy = 6;
        gbc.gridx = 0;
        formPanel.add(confirmPasswordLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(confirmPassword, gbc);

        adminCheckBox.setFont(new Font("Segoe UI", Font.BOLD, 20));
        adminCheckBox.setForeground(new Color(255, 215, 0));
        adminCheckBox.setOpaque(false);
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(adminCheckBox, gbc);

        JLabel adminKeyLabel = new JLabel("Admin Key:");
        adminKeyLabel.setFont(labelFont);
        adminKeyLabel.setForeground(Color.WHITE);
        adminKeyField.setFont(fieldFont);
        adminKeyField.setPreferredSize(new Dimension(450, 40));
        adminKeyLabel.setVisible(false);
        adminKeyField.setVisible(false);
        
        gbc.gridy = 8;
        gbc.gridx = 0;
        gbc.gridwidth = 1;
        gbc.anchor = GridBagConstraints.WEST;
        formPanel.add(adminKeyLabel, gbc);
        gbc.gridx = 1;
        formPanel.add(adminKeyField, gbc);

        adminCheckBox.addActionListener(e -> {
            boolean isSelected = adminCheckBox.isSelected();
            adminKeyLabel.setVisible(isSelected);
            adminKeyField.setVisible(isSelected);
            formPanel.revalidate();
            formPanel.repaint();
        });

        ImageIcon signUpIcon = new ImageIcon("assets/SignUpBtn.png");
        Image scaledSignUp = signUpIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        signUpButton.setIcon(new ImageIcon(scaledSignUp));
        signUpButton.setContentAreaFilled(false);
        signUpButton.setBorderPainted(false);
        signUpButton.setFocusPainted(false);

        ImageIcon backIcon = new ImageIcon("assets/ReturnBtn.png");
        Image scaledBack = backIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        backButton.setIcon(new ImageIcon(scaledBack));
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.add(backButton);
        buttonPanel.add(signUpButton);

        gbc.gridy = 9;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(buttonPanel, gbc);

        mainPanel.add(formPanel);

        strengthUpdateTimer = new Timer(100, e -> updateStrength());
        strengthUpdateTimer.setRepeats(false);

        password.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { strengthUpdateTimer.restart(); }
            public void removeUpdate(DocumentEvent e) { strengthUpdateTimer.restart(); }
            public void changedUpdate(DocumentEvent e) { strengthUpdateTimer.restart(); }
        });

        signUpButton.addActionListener(e -> {
            String emailText = email.getText().trim();
            String usernameText = username.getText().trim();
            String passwordText = new String(password.getPassword());
            String confirmPasswordText = new String(confirmPassword.getPassword());
            boolean wantsAdmin = adminCheckBox.isSelected();
            String adminKey = new String(adminKeyField.getPassword()).trim();

            if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty() || confirmPasswordText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all required fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidEmail(emailText)) {
                JOptionPane.showMessageDialog(this, "Please enter a valid email address.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (passwordText.length() < 4) {
                JOptionPane.showMessageDialog(this, "Password must be at least 4 characters long.", "Error", JOptionPane.ERROR_MESSAGE);
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

            boolean isAdmin = false;
            if (wantsAdmin) {
                if (adminKey.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Please enter the admin key.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!adminKey.equals(ADMIN_SECRET_KEY)) {
                    JOptionPane.showMessageDialog(this, "Invalid admin key.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                isAdmin = true;
            }

            String hashedPassword = hashPassword(passwordText);
            boolean success = FirebaseConnection.sendData(emailText, usernameText, hashedPassword, isAdmin);

            if (success) {
                String successMessage = isAdmin ? 
                    "Admin Account Created Successfully!" : 
                    "Sign Up Successful!";
                JOptionPane.showMessageDialog(this, successMessage, "Success", JOptionPane.INFORMATION_MESSAGE);
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
                passwordStrengthBar.setForeground(new Color(224, 107, 128));
                strengthLabel.setText("Strength: Weak");
            }
            case 2, 3 -> {
                passwordStrengthBar.setForeground(new Color(255, 231, 151));
                strengthLabel.setText("Strength: Moderate");
            }
            case 4 -> {
                passwordStrengthBar.setForeground(new Color(132, 153, 79));
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
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : hash) hex.append(String.format("%02x", b));
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(signUp::new);
    }
}
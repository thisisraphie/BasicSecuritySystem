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
    private JButton SignUpBtn;
    private JButton backButton;
    private mainMenu mainMenu;
    private Timer strengthUpdateTimer;


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
    Font buttonFont = new Font("Segoe UI", Font.BOLD, 18);

    JPanel mainPanel = new JPanel(new GridBagLayout()); 
    mainPanel.setBackground(new Color(3, 52, 110));

    JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 20)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(3, 52, 110));

                int rectWidth = 600;
                int rectHeight = 500;

                int x = (getWidth() - rectWidth) / 2;
                int y = (getHeight() - rectHeight) / 2;

                g2.fillRoundRect(x, y, rectWidth, rectHeight, 40, 40);
                g2.dispose();
            }
        };

        formPanel.setPreferredSize(new Dimension(500, 300));
        formPanel.setBorder(BorderFactory.createEmptyBorder(200, 500, 150, 500));
        formPanel.setOpaque(false);

    JLabel signUpLabel = new JLabel("Sign Up", SwingConstants.CENTER);
    signUpLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
    signUpLabel.setForeground(Color.WHITE);
    signUpLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


    JLabel emailLabel = new JLabel("Email:");
    emailLabel.setFont(labelFont);
    email = new JTextField();
    email.setFont(fieldFont);
    email.setPreferredSize(new Dimension(400, 25));
    emailLabel.setForeground(Color.WHITE);
    email.setCaretColor(Color.BLACK);
    email.setOpaque(true);

    JLabel usernameLabel = new JLabel("Username:");
    usernameLabel.setFont(labelFont);
    username = new JTextField();
    username.setFont(fieldFont);
    username.setPreferredSize(new Dimension(400, 25));
    usernameLabel.setForeground(Color.WHITE);
    username.setCaretColor(Color.BLACK);
    username.setOpaque(true);

    JLabel passwordLabel = new JLabel("Password:");
    passwordLabel.setFont(labelFont);
    password = new JPasswordField();
    password.setFont(fieldFont);
    password.setPreferredSize(new Dimension(400, 25));
    passwordLabel.setForeground(Color.WHITE);
    password.setCaretColor(Color.BLACK);
    password.setOpaque(true);

    passwordStrengthBar = new JProgressBar(0, 4);
    passwordStrengthBar.setValue(0);
    passwordStrengthBar.setStringPainted(false);
    passwordStrengthBar.setPreferredSize(new Dimension(400, 10));
    passwordStrengthBar.setForeground(Color.PINK);

    strengthLabel = new JLabel("Strength: Weak");
    strengthLabel.setFont(new Font("Segoe UI", Font.PLAIN, 20));
    strengthLabel.setForeground(Color.WHITE);

    JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
    confirmPasswordLabel.setFont(labelFont);
    confirmPassword = new JPasswordField();
    confirmPassword.setFont(fieldFont);
    confirmPassword.setPreferredSize(new Dimension(400, 40));
    confirmPasswordLabel.setForeground(Color.WHITE);
    confirmPassword.setCaretColor(Color.BLACK);
    confirmPassword.setOpaque(true);


    ImageIcon SignUpBtn = new ImageIcon("assets/SignUpBtn.png");
        Image scaled = SignUpBtn.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        JButton SignUpButton = new JButton( new ImageIcon(scaled));
        SignUpButton.setContentAreaFilled(false);
        SignUpButton.setBorderPainted(false);
        SignUpButton.setFocusPainted(false);

    ImageIcon backIcon = new ImageIcon("assets/ReturnBtn.png");
        Image scaled2 = backIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        JButton backButton = new JButton(new ImageIcon(scaled2));
        backButton.setContentAreaFilled(false);
        backButton.setBorderPainted(false);
        backButton.setFocusPainted(false);


    formPanel.add(emailLabel);
    formPanel.add(email);
    formPanel.add(usernameLabel);
    formPanel.add(username);
    formPanel.add(passwordLabel);
    formPanel.add(password);
    formPanel.add(new JLabel(""));
    formPanel.add(passwordStrengthBar);
    formPanel.add(new JLabel(""));
    formPanel.add(strengthLabel);
    formPanel.add(confirmPasswordLabel);
    formPanel.add(confirmPassword);

    mainPanel.add(formPanel);
    add(mainPanel);

    JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 40, 20));
    bottomPanel.add(backButton);
    bottomPanel.add(SignUpButton);

    JPanel container = new JPanel(new GridBagLayout());
    container.add(formPanel);

    add(formPanel, BorderLayout.CENTER);
    add(bottomPanel, BorderLayout.SOUTH);



        add(formPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        strengthUpdateTimer = new Timer(100, e -> updateStrength());
        strengthUpdateTimer.setRepeats(false);
        
        password.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                strengthUpdateTimer.restart();
            }

            public void removeUpdate(DocumentEvent e) {
                strengthUpdateTimer.restart();
            }

            public void changedUpdate(DocumentEvent e) {
                strengthUpdateTimer.restart();
            }
        });

        SignUpButton.addActionListener(e -> {
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
                passwordStrengthBar.setForeground(new Color(224, 107, 128));
                strengthLabel.setText("Strength: Weak");
            }
            case 2, 3 -> {
                passwordStrengthBar.setForeground(new Color(255, 231, 151));
                strengthLabel.setText("Strength: Moderate");
            }
            case 4 -> {
                passwordStrengthBar.setForeground(new Color (132, 153, 79));
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

import java.awt.*;
import javax.swing.*;

public class login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private mainMenu mainMenu;
    private JButton loginButton;
    private JButton backButton;
    private int failedAttempts = 0;

    public void setMainMenu(mainMenu mainMenu) {
        this.mainMenu = mainMenu;
    }

    public login() {
        setTitle("Login");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        Font labelFont = new Font("Segoe UI", Font.BOLD, 25);
        Font fieldFont = new Font("Segoe UI", Font.PLAIN, 22);

        JPanel bgPanel = new JPanel(new BorderLayout());
        bgPanel.setBackground(Color.WHITE);
        setContentPane(bgPanel);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

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
        formPanel.setPreferredSize(new Dimension(650, 400));
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(40, 60, 40, 60));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(15, 15, 15, 15);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel loginLabel = new JLabel("Login", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        loginLabel.setForeground(Color.WHITE);
        formPanel.add(loginLabel, gbc);

        gbc.gridy++;
        gbc.gridwidth = 1;
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(labelFont);
        formPanel.add(usernameLabel, gbc);

        gbc.gridx = 1;
        usernameField = new JTextField();
        usernameField.setFont(fieldFont);
        usernameField.setPreferredSize(new Dimension(400, 30));
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(labelFont);
        formPanel.add(passwordLabel, gbc);

        gbc.gridx = 1;
        passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);
        passwordField.setPreferredSize(new Dimension(400, 30));
        formPanel.add(passwordField, gbc);

        gbc.gridy++;
        gbc.gridx = 0;
        backButton = new JButton();
        ImageIcon backIcon = new ImageIcon("assets/ReturnBtn.png");
        Image scaledBack = backIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        backButton.setIcon(new ImageIcon(scaledBack));
        styleButton(backButton);
        formPanel.add(backButton, gbc);

        gbc.gridx = 1;
        loginButton = new JButton();
        ImageIcon loginIcon = new ImageIcon("assets/LoginBtn.png");
        Image scaledLogin = loginIcon.getImage().getScaledInstance(200, 80, Image.SCALE_SMOOTH);
        loginButton.setIcon(new ImageIcon(scaledLogin));
        styleButton(loginButton);
        formPanel.add(loginButton, gbc);

        GridBagConstraints mainGbc = new GridBagConstraints();
        mainGbc.gridx = 0;
        mainGbc.gridy = 0;
        panel.add(formPanel, mainGbc);
        bgPanel.add(panel, BorderLayout.CENTER);

        backButton.addActionListener(e -> {
            if (mainMenu != null) {
                mainMenu.setVisible(true);
            }
            dispose();
        });

        loginButton.addActionListener(e -> handleLogin());

        setVisible(true);
    }

    private void styleButton(JButton button) {
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        FirebaseConnection.getUser(username, new FirebaseConnection.OnUserFetchListener() {
            @Override
            public void onSuccess(String email, String storedHash, boolean isAdmin) {
                String inputHash = hashPassword(password);
                if (inputHash.equals(storedHash)) {
                    String roleMessage = isAdmin ? "Welcome Admin!" : "Login Successful!";
                    JOptionPane.showMessageDialog(login.this, roleMessage);
                    failedAttempts = 0;
                    
                    Landing landingPage = new Landing(username, isAdmin);
                    dispose();
                    landingPage.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(login.this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    handleFailedLogin();
                }
            }

            @Override
            public void onFailure(String error) {
                JOptionPane.showMessageDialog(login.this, "User not found or error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                handleFailedLogin();
            }
        });
    }

    private void handleFailedLogin() {
        failedAttempts++;
        if (failedAttempts >= 5) {
            freezeUI();
        }
    }

    private void freezeUI() {
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
        backButton.setEnabled(false);

        JOptionPane.showMessageDialog(login.this,
                "Too many failed attempts. Locked for 30 seconds.",
                "Locked",
                JOptionPane.WARNING_MESSAGE);

        Timer unfreezeTimer = new Timer(30000, e -> unfreezeUI());
        unfreezeTimer.setRepeats(false);
        unfreezeTimer.start();
    }

    private void unfreezeUI() {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        backButton.setEnabled(true);
        failedAttempts = 0;
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }
}
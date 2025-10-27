import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);

        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 20)) {
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
        formPanel.setPreferredSize(new Dimension(600, 300));
        formPanel.setOpaque(false);  
        formPanel.setBorder(BorderFactory.createEmptyBorder(30, 20, 10, 20)); 

        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setForeground(Color.WHITE);
        usernameLabel.setFont(labelFont);
        JTextField usernameField = new JTextField();
        usernameField.setFont(fieldFont);
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setForeground(Color.WHITE);
        passwordLabel.setFont(labelFont);
        JPasswordField passwordField = new JPasswordField();
        passwordField.setFont(fieldFont);
        JButton backButton = new JButton("Back");
        JButton loginButton = new JButton("Login");

        formPanel.add(usernameLabel);
        formPanel.add(usernameField);
        formPanel.add(passwordLabel);
        formPanel.add(passwordField);
        formPanel.add(backButton);
        formPanel.add(loginButton);

        panel.add(formPanel, new GridBagConstraints());
        add(panel, BorderLayout.CENTER);

        backButton.addActionListener(e -> {
            if (mainMenu != null) {
                mainMenu.setVisible(true);
            }
            dispose();
        });

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter a username and password.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FirebaseConnection.getUser(username, new FirebaseConnection.OnUserFetchListener() {
                @Override
                public void onSuccess(String email, String storedHash) {
                    String inputHash = hashPassword(password);
                    if (inputHash.equals(storedHash)) {
                        JOptionPane.showMessageDialog(login.this, "Login Successful!");
                        failedAttempts = 0; //Reset counter
                        Landing landingPage = new Landing();
                        dispose();
                        landingPage.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(login.this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                        handleFailedLogin(); // Call the new failure method
                    }
                }

                @Override
                public void onFailure(String error) {
                    JOptionPane.showMessageDialog(login.this, "User not found or error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                    handleFailedLogin(); // Call the new failure method
                }
            });
        });

        setVisible(true);
    }

    // Method to handle failures
    private void handleFailedLogin() {
        failedAttempts++;
        if (failedAttempts >= 5) {
            freezeUI(); // Freeze the screen
        }
    }

    // Method to freeze UI
    private void freezeUI() {
        // 1. Disable all fields
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        loginButton.setEnabled(false);
        backButton.setEnabled(false);
        
        // 2. Show a lock message
        JOptionPane.showMessageDialog(login.this, 
            "Too many failed attempts. Locked for 30 seconds.", 
            "Locked", 
            JOptionPane.WARNING_MESSAGE);
            
        // Create a Timer to unfreeze after 30 seconds (30000 milliseconds)
        Timer unfreezeTimer = new Timer(30000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                unfreezeUI(); // Call the unfreeze method
            }
        });
        unfreezeTimer.setRepeats(false); // Make sure it only runs once
        unfreezeTimer.start();
    }

    // method to unfreeze
    private void unfreezeUI() {
        usernameField.setEnabled(true);
        passwordField.setEnabled(true);
        loginButton.setEnabled(true);
        backButton.setEnabled(true);
        failedAttempts = 0; // Reset the counter
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

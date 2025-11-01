import java.awt.*;
import javax.swing.*;

public class Landing extends JFrame {
    private String username;
    private boolean isAdmin;
    
    public Landing(String username, boolean isAdmin) {
        this.username = username;
        this.isAdmin = isAdmin;
        initializeUI();
    }
    
    public Landing() {
        this.username = "Guest";
        this.isAdmin = false;
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("Landing Page - " + (isAdmin ? "Admin Dashboard" : "User Dashboard"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLocationRelativeTo(null);
        
        JPanel bgPanel = new JPanel() {
            Image bg = new ImageIcon("assets/background.jpg").getImage();
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
            }
        };
        bgPanel.setLayout(new BorderLayout());
        setContentPane(bgPanel);
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        topPanel.setOpaque(false);
        
        JLabel welcomeLabel = new JLabel("Welcome, " + username);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        welcomeLabel.setForeground(Color.WHITE);
        topPanel.add(welcomeLabel);
        
        if (isAdmin) {
            JLabel adminBadge = new JLabel("ADMIN");
            adminBadge.setFont(new Font("Segoe UI", Font.BOLD, 20));
            adminBadge.setForeground(new Color(255, 215, 0));
            adminBadge.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
            ));
            topPanel.add(adminBadge);
        }
        
        bgPanel.add(topPanel, BorderLayout.NORTH);
        // Visible only for admins
        if (isAdmin) {
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setOpaque(false);
            
            JPanel adminPanel = new JPanel(new GridLayout(4, 1, 10, 10));
            adminPanel.setBackground(new Color(3, 52, 110, 200));
            adminPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.WHITE, 2),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
            ));
            
            JLabel adminTitle = new JLabel("Admin Controls", SwingConstants.CENTER);
            adminTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
            adminTitle.setForeground(Color.WHITE);
            adminPanel.add(adminTitle);
            
            JButton viewUsersBtn = createStyledButton("View All Users");
            JButton manageUsersBtn = createStyledButton("Manage Users");
            JButton systemSettingsBtn = createStyledButton("System Settings");
            
            adminPanel.add(viewUsersBtn);
            adminPanel.add(manageUsersBtn);
            adminPanel.add(systemSettingsBtn);
            
            centerPanel.add(adminPanel);
            bgPanel.add(centerPanel, BorderLayout.CENTER);
        }
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        bottomPanel.setOpaque(false);
        
        ImageIcon logoutBtn = new ImageIcon("assets/logoutbtn.png");
        Image scaled = logoutBtn.getImage().getScaledInstance(80, 40, Image.SCALE_SMOOTH);
        JButton logoutButton = new JButton(new ImageIcon(scaled));
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        bottomPanel.add(logoutButton);
        
        bgPanel.add(bottomPanel, BorderLayout.SOUTH);
        
        logoutButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                this,
                "Are you sure you want to log out and go back to the main menu?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION
            );
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                mainMenu menu = new mainMenu();
                menu.setVisible(true);
            }
        });
        
        setVisible(true);
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(new Color(0, 100, 200));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.WHITE, 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 120, 220));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(0, 100, 200));
            }
        });
        
        return button;
    }
}
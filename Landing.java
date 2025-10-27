import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Image;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class Landing extends JFrame {

    public Landing() {
        setTitle("Landing Page");
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

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        bottomPanel.setOpaque(false);

        int barHeight = 60;
    
        ImageIcon logoutBtn = new ImageIcon("assets/logoutbtn.png");
        Image scaled = logoutBtn.getImage().getScaledInstance(80, 40, Image.SCALE_SMOOTH);
        JButton logoutButton = new JButton( new ImageIcon(scaled));
        logoutButton.setContentAreaFilled(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setFocusPainted(false);
        logoutButton.setBounds(30, getHeight() - barHeight + 15, 120, 50);

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
}

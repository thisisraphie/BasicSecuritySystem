import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class mainMenu extends JFrame {

    public mainMenu() {

        setTitle("Welcome");
        setSize(250, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel label = new JLabel("Select an option:", JLabel.CENTER);

        JButton loginButton = new JButton("Login");
        JButton signupButton = new JButton("Sign Up");

        panel.add(label);
        panel.add(loginButton);
        panel.add(signupButton);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            login loginFrame = new login();
            loginFrame.setVisible(true);
            loginFrame.setMainMenu(this);
            setVisible(false);
        });

        signupButton.addActionListener(e -> {
            signUp signupFrame = new signUp();
            signupFrame.setVisible(true);
            signupFrame.setMainMenu(this);
            setVisible(false);
        });

        setVisible(true);
    }

    public static void main(String[] args) {
        new mainMenu();
    }
}

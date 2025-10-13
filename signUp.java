import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class signUp extends JFrame {
    private JTextField email;
    private JTextField username;
    private JPasswordField password;
    private JPasswordField confirmPassword;
    private JButton signInButton;

    public signUp() {
        setTitle("Sign Up");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);


        JPanel panel = new JPanel(new GridLayout(5, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        
        JLabel emailLabel = new JLabel("Email:");
        email = new JTextField();

        JLabel usernameLabel = new JLabel("Username:");
        username = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        password = new JPasswordField();

        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPassword = new JPasswordField();

        signInButton = new JButton("Sign In");

        
        panel.add(emailLabel);
        panel.add(email);
        panel.add(usernameLabel);
        panel.add(username);
        panel.add(passwordLabel);
        panel.add(password);
        panel.add(confirmPasswordLabel);
        panel.add(confirmPassword);
        panel.add(new JLabel()); 
        panel.add(signInButton);

        
        add(panel, BorderLayout.CENTER);

        setVisible(true);
    }

    public static void main(String[] args) {
        new signUp();
    }
}

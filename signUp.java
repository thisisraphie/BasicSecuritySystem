import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



public class SignUp extends JFrame {

    private JTextField email;
    private JTextField username;
    private JPasswordField password;
    private JPasswordField confirmPassword;
    private JButton signInButton;


    public SignUp() {

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


        signInButton.addActionListener(e -> {

            String emailText = email.getText().trim();
            String usernameText = username.getText().trim();
            String passwordText = new String(password.getPassword());
            String confirmPasswordText = new String(confirmPassword.getPassword());


            if (!passwordText.equals(confirmPasswordText)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            if (emailText.isEmpty() || usernameText.isEmpty() || passwordText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }


            String hashedPassword = hashPassword(passwordText);
            boolean success = FirebaseConnection.sendData(emailText, usernameText, hashedPassword);


            if (success) {
                JOptionPane.showMessageDialog(this, "Sign Up Successful!", "Success", JOptionPane.INFORMATION_MESSAGE);
                Login loginFrame = new Login();
                loginFrame.setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Failed to send data. Try again.", "Error", JOptionPane.ERROR_MESSAGE);
            }

        });

        setVisible(true);

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

    public static void main(String[] args) {
        new SignUp();
    }

}

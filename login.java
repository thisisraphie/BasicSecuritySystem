import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



public class login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;

    public login() {
        setTitle("Login");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");

        panel.add(usernameLabel);
        panel.add(usernameField);
        panel.add(passwordLabel);
        panel.add(passwordField);
        panel.add(new JLabel()); 
        panel.add(loginButton);

        add(panel, BorderLayout.CENTER);

        loginButton.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()){
                JOptionPane.showMessageDialog(this, "Please Enter a Username and Password", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            FirebaseConnection.getUser(username, new FirebaseConnection.OnUserFetchListener() {
                @Override
                public void onSuccess(String email, String hashedPassword) {
                    if (verifyPassword(password, hashedPassword)) {
                        JOptionPane.showMessageDialog(login.this, "Login Successful!");
                        Landing landingPage = new Landing();
                        dispose();
                        landingPage.setVisible(true);
                    } else {
                        JOptionPane.showMessageDialog(login.this, "Incorrect password.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }

                @Override
                public void onFailure(String error) {
                    JOptionPane.showMessageDialog(login.this, "User not found or error: " + error, "Error", JOptionPane.ERROR_MESSAGE);
                }
            });
        });

        setVisible(true);
    }

    private boolean verifyPassword(String password, String hashedPassword) {
       try{
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash  = digest.digest(password.getBytes(java.nio.charset.StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash){
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString().equals(hashedPassword);
       }catch(Exception e){
        e.printStackTrace();
        return false;
       }
    }
}

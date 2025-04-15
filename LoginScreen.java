import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

public class LoginScreen extends JFrame {
    public LoginScreen() {
        setTitle("Login");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new GridLayout(3, 1));


        JButton clientButton = new JButton("Login as Client");
        clientButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    PopulatieInteract.logUsage("client", "Login", "N/A");
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                openMainWindow("client");
            }
        });

        JButton adminButton = new JButton("Login as Admin");
        adminButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    showAdminLoginDialog();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        add(new JLabel("Select Login Type:", SwingConstants.CENTER));
        add(clientButton);
        add(adminButton);
    }

    private void showAdminLoginDialog() throws SQLException {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        panel.add(new JLabel("Username:"));
        JTextField usernameField = new JTextField();
        panel.add(usernameField);
        panel.add(new JLabel("Password:"));
        JPasswordField passwordField = new JPasswordField();
        panel.add(passwordField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Admin Login", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            if (authenticateAdmin(username, password)) {
                openMainWindow(username);
                PopulatieInteract.logUsage("admin", "Login", "N/A");
            } else {
                JOptionPane.showMessageDialog(this, "Invalid credentials", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean authenticateAdmin(String username, String password) {
        return "admin".equals(username) && "pass".equals(password);
    }

    private void openMainWindow(String currentUser) {
        SwingUtilities.invokeLater(() -> {
            try {
                SwingGUI mainWindow = new SwingGUI(currentUser);
                mainWindow.setVisible(true);

                dispose();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
}
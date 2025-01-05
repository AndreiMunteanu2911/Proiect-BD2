import com.formdev.flatlaf.FlatDarkLaf; // Import the FlatDarkLaf class
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Set the FlatLaf Look and Feel to Dark
        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); // Set to FlatDarkLaf for dark theme
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            LoginScreen loginScreen = new LoginScreen();
            loginScreen.setVisible(true);
        });
    }
}
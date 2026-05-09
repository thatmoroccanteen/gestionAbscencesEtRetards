package app;

import app.ui.LoginFrame;
import app.ui.MainFrame;
import app.ui.UITheme;

import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        UITheme.applyGlobalTheme();

        DatabaseConnection.createTables();

        SwingUtilities.invokeLater(() -> {
            LoginFrame login = new LoginFrame(null);
            login.setVisible(true);
            if (login.isLoginSuccess()) {
                MainFrame frame = new MainFrame();
                frame.setVisible(true);
            } else {
                System.exit(0);
            }
        });
    }
}

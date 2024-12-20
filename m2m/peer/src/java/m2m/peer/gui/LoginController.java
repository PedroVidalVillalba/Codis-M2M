package m2m.peer.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import m2m.peer.PeerMain;
import m2m.peer.User;

public class LoginController {
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);
    }

    @FXML
    private void login() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Los campos no pueden estar vacíos.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            User user = new User(username, password);
            initializeNotifier(user);
            user.login();
            PeerMain.setUser(user);
            PeerMain.setRoot("/gui/Chats.fxml");
        } catch (Exception e) {
            System.err.println("Error en el login: " + e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    @FXML
    private void signUp() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            errorLabel.setText("Los campos no pueden estar vacíos.");
            errorLabel.setVisible(true);
            return;
        }

        try {
            User user = new User(username, password);
            initializeNotifier(user);
            user.signUp();
            PeerMain.setUser(user);
            PeerMain.setRoot("/gui/Chats.fxml");
        } catch (Exception e) {
            System.err.println("Error en el sign up: " + e.getMessage());
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }

    private static void initializeNotifier (User user) {
        if (user == null) return;
        GraphicalNotifier notifier = new GraphicalNotifier();
        user.setNotifier(notifier);
        notifier.setUser(user);
    }

}

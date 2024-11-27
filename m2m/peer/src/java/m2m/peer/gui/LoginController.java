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
            user.login();
            PeerMain.setUser(user);
            PeerMain.setRoot("gui/Chats.fxml");
        } catch (Exception e) {
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
            user.signUp();
            PeerMain.setUser(user);
            PeerMain.setRoot("gui/Chats.fxml");
        } catch (Exception e) {
            errorLabel.setText(e.getMessage());
            errorLabel.setVisible(true);
        }
    }

}

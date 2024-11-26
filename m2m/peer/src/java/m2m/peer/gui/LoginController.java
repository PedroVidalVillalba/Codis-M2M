package m2m.peer.gui;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    private Button loginButton;
    @FXML
    private Button signupButton;
    @FXML
    private Label errorLabel;

    @FXML
    private void initialize() {
        errorLabel.setVisible(false);

        // Botón iniciar sesión
        loginButton.setOnAction(event -> {
            String username = usernameField.getText();
            String password = passwordField.getText();
            try {
                User user = new User(username, password);
                PeerMain.setUser(user);
                PeerMain.setRoot("gui/Chats.fxml");
            } catch (Exception e) {
                errorLabel.setText(e.getMessage());
                errorLabel.setVisible(true);
            }

        });

        // Botón registrarse
        signupButton.setOnAction(event -> {

        });
    }
}

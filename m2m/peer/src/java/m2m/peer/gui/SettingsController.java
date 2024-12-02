package m2m.peer.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import m2m.peer.PeerMain;
import m2m.peer.User;

public class SettingsController {
    @FXML
    private Label userLabel;
    @FXML
    private PasswordField newPasswordField;
    @FXML
    private PasswordField confirmPasswordField;
    @FXML
    private Label changePasswordLabel;
    @FXML
    private PasswordField confirmDeleteUserPassword;
    @FXML
    private Label deleteUserErrorLabel;

    private User user;

    @FXML
    private void initialize() {
        user = PeerMain.getUser();

        userLabel.setText(user.getUsername());
    }

    @FXML
    private void handleChats() {
        PeerMain.setRoot("/gui/Chats.fxml");  // Cambiar a la pestaña de amigos
    }

    @FXML
    private void handleFriends() {
        PeerMain.setRoot("/gui/FriendsManagement.fxml");  // Cambiar a la pestaña de amigos
    }

    @FXML
    private void handleLogout() throws Exception {
        user.logout();
        PeerMain.setUser(null);
        PeerMain.setRoot("/gui/Login.fxml");  // Volver al login
    }

    @FXML
    private void changePassword() {
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (!newPassword.equals(confirmPassword)) {
            changePasswordLabel.setText("Las contraseñas no coinciden");
            if (!changePasswordLabel.getStyleClass().contains("error-label")) {
                changePasswordLabel.getStyleClass().add("error-label");
            }
            return;
        }

//        user.changePassword(newPassword);

        changePasswordLabel.setText("Contraseña cambiada con éxito");
        changePasswordLabel.getStyleClass().remove("error-label");
    }

    @FXML
    private void deleteUser() {
        String password = confirmDeleteUserPassword.getText();
        confirmPasswordField.clear();

        try {
            /* */
//            user.deleteUser(password);
        } catch (Exception exception) {
            deleteUserErrorLabel.setText(exception.getMessage());
        }

        PeerMain.setUser(null);
        PeerMain.setRoot("/gui/Login.fxml");
    }
}

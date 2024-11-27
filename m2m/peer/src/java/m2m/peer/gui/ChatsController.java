package m2m.peer.gui;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import m2m.peer.PeerMain;

public class ChatsController {
    @FXML
    private ListView<String> chatList;
    @FXML
    private ListView<String> messageList;
    @FXML
    private Label chatTitle;
    @FXML
    private TextField messageField;

    @FXML
    private void initialize() {

    }

    private void loadChat(String friendName) {
        if (friendName != null) {
            chatTitle.setText("Chat con " + friendName);
            messageList.getItems().clear();

            // Ejemplo: Cargar mensajes simulados
            messageList.getItems().addAll(
                    "Amigo: Hola!",
                    "Tú: ¿Cómo estás?",
                    "Amigo: Todo bien, gracias."
            );
        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            messageList.getItems().add("Tú: " + message);  // Agregar mensaje al chat
            messageField.clear();
        }
    }

    @FXML
    private void handleFriends() {
        PeerMain.setRoot("FriendsManagement.fxml");  // Cambiar a la pestaña de amigos
    }

    @FXML
    private void handleLogout() {
        PeerMain.setRoot("Login.fxml");  // Volver al login
    }
}

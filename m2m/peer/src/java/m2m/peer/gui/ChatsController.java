package m2m.peer.gui;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import m2m.peer.*;

import java.io.IOException;

public class ChatsController {
    @FXML
    private ListView<Message> currentChatView;
    @FXML
    private TextField messageField;
    @FXML
    private Button friendsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ListView<String> friendsListView;

    private ObservableList<Message> currentChat;
    private String currentFriendChat;
    private User user;
    private Notifier notifier;


    @FXML
    private void initialize() throws Exception {
        user = PeerMain.getUser();
        notifier = user.getNotifier();

        friendsListView.setItems(PeerMain.getActiveFriends());
        currentFriendChat = null;

        currentChatView.setCellFactory(new Callback<>() {
            @Override
            public ListCell<Message> call(ListView<Message> messageListView) {
                return new ListCell<>() {
                    @Override
                    protected void updateItem(Message message, boolean empty) {
                        super.updateItem(message, empty);
                        if (empty || message == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            /* Cargar el FXML del contenedor del mensaje */
                            HBox messageContainer = loadMessageContainer(message);
                            setGraphic(messageContainer);
                        }
                    }

                    private HBox loadMessageContainer(Message message) {
                        HBox messageContainer = new HBox();
                        /* Cargar el FXML para el contenedor del mensaje */
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/MessageContainer.fxml"));
                            loader.setController(this);
                            messageContainer = loader.load();

                            /* Establecer el contenido del mensaje */
                            Label label = switch (message.type()) {
                                case SENT -> (Label) messageContainer.lookup("#receivedMessage");
                                case RECEIVED -> (Label) messageContainer.lookup("#sentMessage");
                            };
                            label.setText(message.message());
                        } catch (IOException exception) {
                            System.err.println(exception.getMessage());
                        }
                        return messageContainer;
                    }
                };
            }
        });
    }

    @FXML
    private void loadChat() {
        String friendName = friendsListView.getSelectionModel().getSelectedItem();
        if (friendName == null) return;

        try {
            user.greet(friendName);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }

        currentFriendChat = friendName;
        currentChat = FXCollections.observableList(user.getChat(currentFriendChat));
        currentChatView.setItems(currentChat);

        notifier.setNotifyMessage(message -> Platform.runLater(() -> currentChat.add(message)));
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (message.isEmpty()) return;
        try {
            user.sendMessage(currentFriendChat, message);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        } finally {
            messageField.clear();
        }
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
}

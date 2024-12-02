package m2m.peer.gui;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import m2m.peer.*;

import java.io.IOException;
import java.util.HashSet;

public class ChatsController {
    @FXML
    private ListView<Message> currentChatView;
    @FXML
    private TextField messageField;
    @FXML
    private ListView<String> friendsListView;

    private ObservableList<Message> currentChat;
    private String currentFriendChat;
    private User user;
    private ObservableSet<String> messagePending;
    private Notifier notifier;


    @FXML
    private void initialize() {
        user = PeerMain.getUser();
        notifier = user.getNotifier();

        friendsListView.setItems(PeerMain.getActiveFriends());
        currentFriendChat = null;
        /* Añadir una lista de los amigos con mensajes pendientes y un listener para actualizar la vista */
        messagePending = FXCollections.observableSet(new HashSet<>());
        messagePending.addListener((SetChangeListener<String>) change -> {
            if (change.wasAdded() || change.wasRemoved()) {
                Platform.runLater(() -> friendsListView.refresh());
            }
        });

        currentChatView.setCellFactory(messageListView -> new ListCell<>() {
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
                        case RECEIVED -> (Label) messageContainer.lookup("#receivedMessage");
                        case SENT -> (Label) messageContainer.lookup("#sentMessage");
                    };
                    label.setText(message.message());
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
                return messageContainer;
            }
        });

        friendsListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(String friend, boolean empty) {
                super.updateItem(friend, empty);
                if (empty || friend == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(friend);
                    if (messagePending.contains(friend)) {
                        if (!getStyleClass().contains("message-pending")) {
                            getStyleClass().add("message-pending");
                        }
                    } else {
                        getStyleClass().remove("message-pending");
                    }
                }
            }
        });

        /* Notificación por defecto si aún no entró a ningún chat */
        notifier.setNotifyMessage((message, friend) -> Platform.runLater(() -> {
            if (friendsListView.getItems().contains(friend)) {
                messagePending.add(friend);
            }

            if (!friendsListView.getScene().getWindow().isShowing()) {
                try {
                    Runtime.getRuntime().exec(new String[] {"notify-send", friend + ": " + message.message()});
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
            }
        }));
    }

    @FXML
    private void loadChat() {
        String friendName = friendsListView.getSelectionModel().getSelectedItem();
        if (friendName == null) return;

        /* TODO: aquí se hace un handshake nuevo cada vez que se abre un chat, incluso que el otro ya estuviera saludado
             No es necesariamente malo, porque hace que se cambie todavía más a menudo de claves, pero habría que pensar si hace falta */
        try {
            user.greet(friendName);
        } catch (Exception exception) {
            System.err.println(exception.getMessage());
        }

        currentFriendChat = friendName;
        currentChat = FXCollections.observableArrayList(user.getChat(currentFriendChat));
        currentChatView.setItems(currentChat);
        messagePending.remove(currentFriendChat);

        notifier.setNotifyMessage((message, friend) -> Platform.runLater(() -> {
            if (currentFriendChat != null && currentFriendChat.equals(friend)) {
                currentChat.add(message);
            } else if (friendsListView.getItems().contains(friend)) {
                messagePending.add(friend);
            }

            if (!friendsListView.getScene().getWindow().isShowing()) {
                try {
                    Runtime.getRuntime().exec(new String[] {"notify-send", friend + ": " + message.message()});
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
            }
        }));
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

    @FXML
    private void handleSettings() {
        PeerMain.setRoot("/gui/Settings.fxml");
    }
}

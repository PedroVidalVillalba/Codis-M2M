package m2m.peer.gui;

import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import m2m.peer.Message;
import m2m.peer.MessageType;
import m2m.peer.PeerMain;
import m2m.shared.Peer;

public class ChatsController {
    @FXML
    private ListView<String> receivedMessages;
    @FXML
    private ListView<String> sentMessages;
    @FXML
    private TextField messageField;
    @FXML
    private Button friendsButton;
    @FXML
    private Button logoutButton;
    @FXML
    private ListView<String> friendsList;

    private ObservableList<String> activeFriends;

    private ObservableList<String> received;
    private ObservableList<String> sent;
    private ListChangeListener<Message> chatListener;
    private String currentFriendChat;


    @FXML
    private void initialize() {
        activeFriends = FXCollections.observableArrayList(PeerMain.getUser().getActiveFriends().keySet());
        // Sincronización entre el HashMap de amigos conectados de User y la lista de amigos con los que se puede chatear
        PeerMain.getUser().getActiveFriends().addListener((MapChangeListener<? super String, ? super Peer>) (change) -> {
            if (change.wasAdded() && change.getKey() != null) {
                activeFriends.add(change.getKey());
            } else if (change.wasRemoved() && change.getKey() != null) {
                activeFriends.remove(change.getKey());
            }
        });
        friendsList.setItems(FXCollections.observableArrayList(activeFriends));

        // Al principio, no se pone nada en mensajes enviados y recibidos
        receivedMessages.setItems(FXCollections.observableArrayList());
        sentMessages.setItems(FXCollections.observableArrayList());
        currentFriendChat = null;

        // Cambiar de chat al seleccionar un amigo
        friendsList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                loadChat(newValue);
            }
        });

        chatListener = change-> {
            if (change.wasAdded() && change.getAddedSubList() != null) {
                for (Message message : change.getAddedSubList()) {
                    if (message.type() == MessageType.SENT) {
                        sent.add(message.message());
                    } else {
                        received.add(message.message());
                    }
                }
            }
        };

    }

    private void loadChat(String friendName) {
        if (currentFriendChat != null) {
            PeerMain.getUser().getChat(currentFriendChat).removeListener(chatListener);
        }
        if (friendName != null) {
            sent.clear();
            received.clear();
            currentFriendChat = friendName;

            ObservableList<Message> messages = PeerMain.getUser().getChat(friendName);
            for (Message message : messages) {
                if (message.type() == MessageType.SENT) {
                    sent.add(message.message());
                } else {
                    received.add(message.message());
                }
            }
            messages.addListener(chatListener);
            sentMessages.setItems(FXCollections.observableArrayList(sent));
            receivedMessages.setItems(FXCollections.observableArrayList(received));


        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                PeerMain.getUser().sendMessage(currentFriendChat, message);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            } finally {
                messageField.clear();
            }
        }
    }

    @FXML
    private void handleFriends() {
        PeerMain.setRoot("gui/FriendsManagement.fxml");  // Cambiar a la pestaña de amigos
    }

    @FXML
    private void handleLogout() {
        PeerMain.setRoot("gui/Login.fxml");  // Volver al login
    }
}

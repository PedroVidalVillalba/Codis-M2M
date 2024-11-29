package m2m.peer.gui;

import javafx.application.Platform;
import javafx.collections.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import m2m.peer.*;

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
    private ListView<String> friendsListView;

    private ObservableList<String> activeFriends;

    private ObservableList<String> received;
    private ObservableList<String> sent;
    private String currentFriendChat;
    private User user;
    private Notifier notifier;


    @FXML
    private void initialize() throws Exception {
        friendsButton.setText("󰣐");
        logoutButton.setText("󰠜");
        user = PeerMain.getUser();
        activeFriends = PeerMain.getActiveFriends();
        sent = PeerMain.getSent();
        received = PeerMain.getReceived();

        friendsListView.setItems(activeFriends);
        sentMessages.setItems(sent);
        receivedMessages.setItems(received);
        currentFriendChat = null;

        for(String friend: activeFriends) {
            user.greet(friend);
        }

    }

    @FXML
    private void loadChat() {
        String friendName = friendsListView.getSelectionModel().getSelectedItem();
        if (friendName != null) {
            currentFriendChat = friendName;
            received = FXCollections.observableArrayList();
            sent = FXCollections.observableArrayList();


            for (Message message : PeerMain.getUser().getChat(friendName)) {
                if (message.type() == MessageType.SENT) {
                    sent.add(message.message());
                } else {
                    received.add(message.message());
                }
            }
            sentMessages.setItems(FXCollections.observableArrayList(sent));
            receivedMessages.setItems(FXCollections.observableArrayList(received));


        }
    }

    @FXML
    private void handleSendMessage() {
        String message = messageField.getText();
        if (!message.isEmpty()) {
            try {
                user.sendMessage(currentFriendChat, message);
                sent.add(message);
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
    private void handleLogout() throws Exception {
        user.logout();
        PeerMain.setUser(null);
        PeerMain.setRoot("gui/Login.fxml");  // Volver al login
    }
}

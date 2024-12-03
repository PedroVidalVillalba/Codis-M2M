package m2m.peer.gui;

import m2m.peer.Message;
import m2m.peer.Notifier;
import m2m.peer.PeerMain;
import m2m.peer.User;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;
import java.io.IOException;

public class GraphicalNotifier implements Notifier {
    private User user;
    private ObservableList<String> activeFriends;
    private ObservableList<String> friends;
    private ObservableList<String> friendRequests;
    private ObservableSet<String> messagePending;
    private ObservableList<Message> currentChat;
    private String currentFriendChat;

    public void setUser(User user) {
        this.user = user;
    }

    public void setActiveFriends(ObservableList<String> activeFriends){
        this.activeFriends = activeFriends;
    }

    public void setFriends(ObservableList<String> friends) {
        this.friends = friends;
    }

    public void setFriendRequests(ObservableList<String> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public void setMessagePending(ObservableSet<String> messagePending) {
        this.messagePending = messagePending;
    }

    public void setCurrentChat(String friendName, ObservableList<Message> currentChat) {
        this.currentFriendChat = friendName;
        this.currentChat = currentChat;
    }

    public static void systemNotification(String notification) {
        try {
            Runtime.getRuntime().exec(new String[] {"notify-send", notification});
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    @Override
    public void notifyAddActiveFriend(String friendName) {
        systemNotification(friendName + " se ha conectado");
        Platform.runLater(() -> {
            if (activeFriends != null) {
                activeFriends.add(friendName);
            }
            refreshFriends();
        });
    }

    @Override
    public void notifyRemoveActiveFriend(String friendName) {
        systemNotification(friendName + " se ha desconectado");
        Platform.runLater(() -> {
            if (activeFriends != null) {
                activeFriends.remove(friendName);
                if (currentFriendChat != null && currentFriendChat.equals(friendName)) {
                    currentChat.clear();
                    currentFriendChat = null;
                    currentChat = null;
                }
                if (messagePending != null) {
                    messagePending.remove(friendName);
                }
            }
            refreshFriends();
        });
    }

    @Override
    public void notifyAllFriendsConnected(String allFriends) {
        if (user == null) return;
        Platform.runLater(() -> {
            String pendingRequestsMessage ="";
            try {
                if (!user.searchPendingRequests().isEmpty()) {
                    pendingRequestsMessage = "Hay nuevas solicitudes de amistad";
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            String notification = (allFriends.isEmpty() ? "No hay amigos conectados. " : "Amigos conectados: " + allFriends) + pendingRequestsMessage;
            systemNotification(notification);
            /* No hace falta actualizar active friends, porque la lista de amigos activos
             * ya se crea en el controlador llena */
        });
    }

    @Override
    public void notifyMessage(Message message, String friendName) {
        Platform.runLater(() -> {
            if (currentChat != null && currentFriendChat != null && currentFriendChat.equals(friendName)) {
                currentChat.add(message);
            } else if (activeFriends != null && messagePending != null && activeFriends.contains(friendName)) {
                messagePending.add(friendName);
            }
            if (!PeerMain.windowFocused()) {
                systemNotification(friendName + ": " + message.message());
            }
        });
    }

    @Override
    public void notifyNewFriendRequest(String personName) {
        systemNotification("Nueva petición de amistad de " + personName);
        Platform.runLater(() -> {
            if (friendRequests != null) {
                try {
                    friendRequests.setAll(user.searchPendingRequests());
                } catch (Exception exception) {
                    System.err.println(exception.getMessage());
                }
            }
        });
    }

    public void refreshFriends() {
        Platform.runLater(() -> {
            if (friends != null) {
                try {
                    friends.setAll(user.searchFriends());
                } catch (Exception exception) {
                    System.err.println(exception.getMessage());
                }
            }
        });
    }
}

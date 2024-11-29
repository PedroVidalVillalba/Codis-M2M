package m2m.peer.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import m2m.peer.PeerMain;
import m2m.peer.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsManagementController {
    @FXML
    public Button chatsButton;
    @FXML
    public Button logoutButton;
    @FXML
    private ListView<HBox> friendsListView; // Cada amigo es un HBox para icono y botones
    @FXML
    private TextField searchField;
    @FXML
    private ListView<HBox> searchResultsListView;
    @FXML
    private ListView<HBox> friendRequestsListView;

    @FXML
    private Label friendsErrorLabel;
    @FXML
    private Label searchErrorLabel;
    @FXML
    private Label requestsErrorLabel;


    private ObservableList<String> activeFriends;
    private ObservableList<HBox> friends;
    private ObservableList<HBox> searchResults;
    private ObservableList<HBox> friendRequests;
    private User user;


    public void initialize() throws Exception {
        user = PeerMain.getUser();
        activeFriends = PeerMain.getActiveFriends();
        friends = PeerMain.getFriends();
        searchResults = FXCollections.observableArrayList();
        friendRequests = FXCollections.observableArrayList();


        initializeFriends();
        initializePendingRequests();


        // Asociar listas observables con las ListView
        friendsListView.setItems(friends);
        friendRequestsListView.setItems(friendRequests);



    }

    public void searchForPeople() {
        String pattern = searchField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            searchErrorLabel.setVisible(false);
            searchResults.clear();
            boolean foundSomething = false;
            try {
                List<String> people = user.searchUsers(pattern);
                for (String person : people) {
                    if (!Objects.equals(person, user.getUsername())) {
                        foundSomething = true;
                        Label nameLabel = new Label(person);
                        nameLabel.setStyle("-fx-font-size: 16px;");
                        nameLabel.setPrefWidth(90);
                        Button requestButton = new Button("Solicitar amistad");
                        requestButton.setOnAction(e -> {
                            try {
                                user.requestFriendship(person);
                            } catch (Exception exception) {
                                searchErrorLabel.setText(exception.getMessage());
                                searchErrorLabel.setVisible(true);
                                System.err.println("Error al enviar solicitud de amistad: " + exception.getMessage());
                            }
                        });

                        HBox box = new HBox(10, nameLabel, requestButton);
                        searchResults.add(box);
                    }
                }
                searchResultsListView.setItems(FXCollections.observableArrayList(searchResults));
            } catch (Exception e) {
                System.err.println("Error al buscar personas: " + e.getMessage());
                searchErrorLabel.setText(e.getMessage());
                searchErrorLabel.setVisible(true);
                foundSomething = true;
            }

            if(!foundSomething) {
                searchErrorLabel.setText("Búsqueda sin resultados");
                searchErrorLabel.setVisible(true);
            }
        }

    }

    public void handleLogout() throws Exception {
        user.logout();
        PeerMain.setUser(null);
        PeerMain.setRoot("gui/Login.fxml");  // Volver al login
    }

    public void handleChats() {
        PeerMain.setRoot("gui/Chats.fxml");  // Cambiar a la pestaña de chats
    }

    private void initializeFriends() throws Exception {
        List<String> remainingFriends = new ArrayList<>(user.searchFriends());
        List<String> addedFriends = checkAddedFriends();
        // No tocamos los amigos que ya están añadidos a la lista
        remainingFriends.removeAll(addedFriends);
        for (String friendName: remainingFriends) {
            Label nameLabel = new Label(friendName);
            nameLabel.setStyle("-fx-font-size: 16px;");
            nameLabel.setPrefWidth(90);
            Label statusLabel = new Label("○");
            Button removeButton = new Button("Eliminar");
            removeButton.setOnAction(e -> {
                try {
                    user.removeFriendship(friendName);
                } catch (Exception exception) {
                    System.err.println("Error al eliminar amigo: " + exception.getMessage());
                    friendsErrorLabel.setText(exception.getMessage());
                    friendsErrorLabel.setVisible(true);
                }
            });
            HBox friendBox = new HBox(10, nameLabel, statusLabel, removeButton);
            friends.add(friendBox);


        }
    }

    private void initializePendingRequests() throws Exception {
        for (String person: user.searchPendingRequests()) {
            Label nameLabel = new Label(person);
            nameLabel.setStyle("-fx-font-size: 16px;");
            nameLabel.setPrefWidth(140);
            Button acceptButton = new Button("✔");
            Button rejectButton = new Button("✖");

            acceptButton.setOnAction(e -> {
                try {
                    user.acceptFriendship(person);
                    friendRequests.removeIf(hbox -> {
                        Label label = (Label) hbox.getChildren().getFirst();
                        return label.getText().equals(person);
                    });
                } catch (Exception exception) {
                    System.err.println("Error al aceptar solicitud de amistad: " + exception.getMessage());
                    requestsErrorLabel.setText(exception.getMessage());
                    requestsErrorLabel.setVisible(true);
                }
            });

            rejectButton.setOnAction(e -> {
                try {
                    user.rejectFriendship(person);
                    friendRequests.removeIf(hbox -> {
                        Label label = (Label) hbox.getChildren().getFirst();
                        return label.getText().equals(person);
                    });
                } catch (Exception exception) {
                    System.err.println("Error al rechazar solicitud de amistad: " + exception.getMessage());
                    requestsErrorLabel.setText(exception.getMessage());
                    requestsErrorLabel.setVisible(true);
                }
            });

            HBox requestBox = new HBox(10, nameLabel, acceptButton, rejectButton);
            friendRequests.add(requestBox);
        }
    }




    private List<String> checkAddedFriends() {
        List<String> addedFriends = new ArrayList<>();
        for (HBox hbox : friends) {
            Label friendName = (Label) hbox.getChildren().getFirst();
            // Compara el texto del primer elemento con el nombre del amigo
            addedFriends.add(friendName.getText());
        }
        return addedFriends;
    }
}

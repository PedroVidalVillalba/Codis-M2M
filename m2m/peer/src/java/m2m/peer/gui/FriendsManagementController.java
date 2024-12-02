package m2m.peer.gui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import m2m.peer.Notifier;
import m2m.peer.PeerMain;
import m2m.peer.User;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FriendsManagementController {
    @FXML
    private ListView<HBox> friendsListView; // Cada amigo es un HBox para nombre y botones
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


    private ObservableList<HBox> friends;
    private ObservableList<HBox> searchResults;
    private ObservableList<HBox> friendRequests;
    private User user;


    public void initialize() throws Exception {
        user = PeerMain.getUser();
        friends = PeerMain.getFriends();
        searchResults = FXCollections.observableArrayList();
        friendRequests = FXCollections.observableArrayList();

        initializeFriends();
        initializePendingRequests();

        // Actualizar el comportamiento del notifier para actualizar las listas de amigos y solicitudes pendientes cuando sea necesario
        Notifier notifier = user.getNotifier();
        notifier.setRefreshFriends(friendName -> Platform.runLater(() -> {
            try {
                initializeFriends();
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
            }
        }));
        notifier.setRefreshFriendRequests(friendName -> Platform.runLater(() -> {
            try {
                initializePendingRequests();
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
            }
        }));



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
                                searchErrorLabel.setText("Solicitud enviada a " + person);
                                searchErrorLabel.setVisible(true);
                            } catch (Exception exception) {
                                searchErrorLabel.setText(exception.getMessage());
                                searchErrorLabel.setVisible(true);
                                System.err.println("Error al enviar solicitud de amistad: " + exception.getMessage());
                            }
                        });
                        Region spacer = new Region();
                        HBox.setHgrow(spacer, Priority.ALWAYS);

                        HBox box = new HBox(10, nameLabel, spacer, requestButton);
                        box.setAlignment(Pos.CENTER);
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

    @FXML
    private void handleLogout() throws Exception {
        user.logout();
        PeerMain.setUser(null);
        PeerMain.setRoot("/gui/Login.fxml");  // Volver al login
    }

    @FXML
    private void handleChats() {
        PeerMain.setRoot("/gui/Chats.fxml");  // Cambiar a la pestaña de chats
    }

    @FXML
    private void handleSettings() {
        PeerMain.setRoot("/gui/Settings.fxml"); // Cambiar a la pestaña de ajustes
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
            Circle status = new Circle();
            status.setRadius(10);
            status.setFill(Color.TRANSPARENT);
            status.setStroke(Color.GRAY);
            status.setStrokeWidth(2);
            Button removeButton = new Button("󰀒");
            removeButton.setOnAction(e -> {
                try {
                    user.removeFriendship(friendName);
                    friends.removeIf(hbox -> {
                        Label label = (Label) hbox.getChildren().getFirst();
                        return label.getText().equals(friendName);
                    });
                } catch (Exception exception) {
                    System.err.println("Error al eliminar amigo: " + exception.getMessage());
                    friendsErrorLabel.setText(exception.getMessage());
                    friendsErrorLabel.setVisible(true);
                }
            });
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox friendBox = new HBox(10, nameLabel, spacer, status, removeButton);
            friendBox.setAlignment(Pos.CENTER);
            friends.add(friendBox);
        }
    }

    private void initializePendingRequests() throws Exception {
        friendRequests.clear();
        for (String person: user.searchPendingRequests()) {
            Label nameLabel = new Label(person);
            nameLabel.setStyle("-fx-font-size: 16px;");
            nameLabel.setPrefWidth(90);
            Button acceptButton = new Button("");
            Button rejectButton = new Button("❌");

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
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            HBox requestBox = new HBox(10, nameLabel, spacer, acceptButton, rejectButton);
            requestBox.setAlignment(Pos.CENTER);
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

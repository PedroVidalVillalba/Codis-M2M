package m2m.peer.gui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Circle;
import m2m.peer.PeerMain;
import m2m.peer.User;

import java.io.IOException;
import java.util.List;

public class FriendsManagementController {
    @FXML
    private TextField searchField;
    @FXML
    private ListView<String> friendsListView; // Cada amigo es un HBox para nombre y botones
    @FXML
    private ListView<String> searchResultsListView;
    @FXML
    private ListView<String> friendRequestsListView;

    @FXML
    private Label friendsErrorLabel;
    @FXML
    private Label searchErrorLabel;
    @FXML
    private Label requestsErrorLabel;

    private ObservableList<String> friends;
    private ObservableList<String> searchResults;
    private ObservableList<String> friendRequests;
    private User user;


    public void initialize() throws Exception {
        user = PeerMain.getUser();
        friends = FXCollections.observableArrayList(user.searchFriends());
        searchResults = FXCollections.observableArrayList();
        friendRequests = FXCollections.observableArrayList(user.searchPendingRequests());

        /* Asociar vistas con sus correspondientes listas observables */
        friendsListView.setItems(friends);
        searchResultsListView.setItems(searchResults);
        friendRequestsListView.setItems(friendRequests);

        friendsListView.setCellFactory(friendsView -> new ListCell<>() {
            @Override
            protected void updateItem(String friendName, boolean empty) {
                super.updateItem(friendName, empty);
                if (empty || friendName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox friendContainer = loadFriendContainer(friendName);
                    setGraphic(friendContainer);
                }
            }

            private HBox loadFriendContainer(String friendName) {
                HBox friendContainer = new HBox();
                /* Cargar el FXML para el contenedor del amigo */
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/FriendContainer.fxml"));
                    loader.setController(this);
                    friendContainer = loader.load();

                    /* Establecer el contenido de la columna de amigos */
                    Label friendNameLabel = (Label) friendContainer.lookup("#friendName");
                    friendNameLabel.setText(friendName);
                    Circle status = (Circle) friendContainer.lookup("#status");
                    if (user.getActiveFriends().containsKey(friendName)) {
                        status.getStyleClass().remove("empty-circle");
                        status.getStyleClass().add("filled-circle");
                    } else {
                        status.getStyleClass().remove("filled-circle");
                        status.getStyleClass().add("empty-circle");
                    }
                    Button removeFriendButton = (Button) friendContainer.lookup("#removeFriendButton");
                    removeFriendButton.setOnAction(e -> {
                        try {
                            user.removeFriendship(friendName);
                            /* Eliminarlo de la lista observable para que se actualice la vista */
                            friends.remove(friendName);
                        } catch (Exception exception) {
                            friendsErrorLabel.setText(exception.getMessage());
                        }
                    });
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
                return friendContainer;
            }
        });

        searchResultsListView.setCellFactory(searchView -> new ListCell<>() {
            @Override
            protected void updateItem(String searchedName, boolean empty) {
                super.updateItem(searchedName, empty);
                if (empty || searchedName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox searchContainer = loadSearchContainer(searchedName);
                    setGraphic(searchContainer);
                }
            }

            private HBox loadSearchContainer(String searchedName) {
                HBox searchContainer = new HBox();
                /* Cargar el FXML para el contenedor del amigo */
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/SearchContainer.fxml"));
                    loader.setController(this);
                    searchContainer = loader.load();

                    /* Establecer el contenido de la columna de amigos */
                    Label searchedNameLabel = (Label) searchContainer.lookup("#searchedName");
                    searchedNameLabel.setText(searchedName);

                    Button requestButton = (Button) searchContainer.lookup("#requestButton");
                    requestButton.setOnAction(e -> {
                        try {
                            user.requestFriendship(searchedName);
                            searchResults.remove(searchedName);
                            searchErrorLabel.setText("Solicitud enviada a " + searchedName);
                        } catch (Exception exception) {
                            searchErrorLabel.setText(exception.getMessage());
                        }
                    });
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
                return searchContainer;
            }
        });

        friendRequestsListView.setCellFactory(requestsView -> new ListCell<>() {
            @Override
            protected void updateItem(String requesterName, boolean empty) {
                super.updateItem(requesterName, empty);
                if (empty || requesterName == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox friendContainer = loadRequestContainer(requesterName);
                    setGraphic(friendContainer);
                }
            }

            private HBox loadRequestContainer(String requesterName) {
                HBox requestContainer = new HBox();
                /* Cargar el FXML para el contenedor del amigo */
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/RequestContainer.fxml"));
                    loader.setController(this);
                    requestContainer = loader.load();

                    /* Establecer el contenido de la columna de amigos */
                    Label requesterNameLabel = (Label) requestContainer.lookup("#requesterName");
                    requesterNameLabel.setText(requesterName);

                    Button acceptButton = (Button) requestContainer.lookup("#acceptButton");
                    acceptButton.setOnAction(e -> {
                        try {
                            user.acceptFriendship(requesterName);
                            friendRequests.remove(requesterName);
                        } catch (Exception exception) {
                            requestsErrorLabel.setText(exception.getMessage());
                        }
                    });

                    Button rejectButton = (Button) requestContainer.lookup("#rejectButton");
                    rejectButton.setOnAction(e -> {
                        try {
                            user.rejectFriendship(requesterName);
                            friendRequests.remove(requesterName);
                        } catch (Exception exception) {
                            requestsErrorLabel.setText(exception.getMessage());
                        }
                    });
                } catch (IOException exception) {
                    System.err.println(exception.getMessage());
                }
                return requestContainer;
            }
        });

        // Actualizar el comportamiento del notifier para actualizar las listas de amigos y solicitudes pendientes cuando sea necesario
        GraphicalNotifier notifier = (GraphicalNotifier) user.getNotifier();
        notifier.setFriends(friends);
        notifier.setFriendRequests(friendRequests);
    }

    public void searchForPeople() {
        String pattern = searchField.getText();
        if (pattern != null && !pattern.isEmpty()) {
            searchErrorLabel.setText("");
            searchResults.clear();
            try {
                List<String> people = user.searchUsers(pattern);
                /* Eliminar al propio usuario si es que estaba */
                people.remove(user.getUsername());
                searchResults.setAll(people);
                if (people.isEmpty()) {
                    searchErrorLabel.setText("Búsqueda sin resultados");
                }
            } catch (Exception e) {
                searchErrorLabel.setText(e.getMessage());
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

}

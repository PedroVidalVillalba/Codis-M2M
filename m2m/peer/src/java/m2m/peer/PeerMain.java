package m2m.peer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import m2m.peer.gui.NotifierGUI;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class PeerMain extends Application {
    private static Stage primaryStage;
    private static User user;

    private static ObservableList<HBox> friends; // Cada amigo es un HBox para nombre y botones
    private static ObservableList<String> activeFriends;

    public PeerMain() {}

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/Login.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage = stage;
        stage.setOnCloseRequest(event -> {
            if (user != null) {
                try {
                    user.logout();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            System.exit(0);
        });

        stage.setTitle("M2M");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) {
        try {
            primaryStage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(PeerMain.class.getResource(fxml)))));
        } catch (IOException exception) {
            System.err.println(exception.getMessage());
        }
    }

    public static void setUser(User user) {
        PeerMain.user = user;
    }

    public static User getUser() {
        return user;
    }

    public static ObservableList<HBox> getFriends() {
        return friends;
    }

    public static ObservableList<String> getActiveFriends() {
        return activeFriends;
    }

    public static void initializeNotifier (User user) {
        if (user == null) return;
        Notifier notifier = new NotifierGUI();
        user.setNotifier(notifier);
        activeFriends = FXCollections.observableArrayList();
        friends = FXCollections.observableArrayList();

        // Configuración de notificaciones de conexión y desconexión de amigos
        notifier.setNotifyAddActiveFriend(friendName -> Platform.runLater(() -> {
            try {
                Runtime.getRuntime().exec(new String[] {"notify-send", friendName + " se ha conectado"});
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }

            activeFriends.add(friendName);
            newActiveFriend(friendName);
        }));

        notifier.setNotifyRemoveActiveFriend(friendName -> Platform.runLater(() -> {
            activeFriends.remove(friendName);
            try {
                Runtime.getRuntime().exec(new String[] {"notify-send", friendName + " se ha desconectado"});
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }
            // Se quita de amigos y se refresca la lista. Hay dos casos: realmente se ha desconectado, o ha eliminado al usuario de amigos, por eso hay que refrescar
            friends.removeIf(hbox -> {
                Label label = (Label) hbox.getChildren().getFirst();
                return label.getText().equals(friendName);
            });

            notifier.refreshFriends(friendName);
        }));

        notifier.setNotifyAllFriendsConnected(allFriends -> Platform.runLater(() -> {
            String pendingRequestsMessage ="";
            try {
                if (!user.searchPendingRequests().isEmpty()) {
                    pendingRequestsMessage = "Hay nuevas solicitudes de amistad";
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }

            try {
                if(allFriends.isEmpty()) {
                    Runtime.getRuntime().exec(new String[] {"notify-send", "No hay amigos conectados", pendingRequestsMessage});
                } else {
                    Runtime.getRuntime().exec(new String[] {"notify-send", "Amigos conectados: " + allFriends, pendingRequestsMessage});
                    String[] friendNames = allFriends.split(", ");
                    System.out.println(Arrays.toString(friendNames));
                    for (String friendName : friendNames) {
                        activeFriends.add(friendName);
                        newActiveFriend(friendName);
                    }
                }
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }


        }));

        notifier.setNotifyNewFriendRequest(personName -> Platform.runLater(() -> {
            try {
                Runtime.getRuntime().exec(new String[] {"notify-send", "Nueva petición de amistad de " + personName});
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }
            notifier.refreshFriendRequests(personName);
        }));

    }

    // Elimina al amigo de la lista (por si apareciese como desconectado), y lo añade como conectado
    private static void newActiveFriend(String friendName) {
        friends.removeIf(hbox -> {
            Label label = (Label) hbox.getChildren().getFirst();
            return label.getText().equals(friendName);
        });

        Label nameLabel = new Label(friendName);
        nameLabel.setStyle("-fx-font-size: 16px;");
        nameLabel.setPrefWidth(90); // Ancho mínimo
        Circle status = new Circle();
        status.setRadius(11);
        status.setFill(Color.GREEN);
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
            }
        });
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox friendBox = new HBox(10, nameLabel, spacer, status, removeButton);
        friendBox.setAlignment(Pos.CENTER);
        friends.addFirst(friendBox);
    }

    public static void main(String[] args) {
        launch(args);
    }
}

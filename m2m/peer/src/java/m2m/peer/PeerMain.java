package m2m.peer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import m2m.peer.gui.NotifierGUI;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

public class PeerMain extends Application {
    private static Stage primaryStage;
    private static User user;

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

    public static boolean windowFocused() {
        return primaryStage.isFocused();
    }

    public static void setUser(User user) {
        PeerMain.user = user;
    }

    public static User getUser() {
        return user;
    }

    public static void initializeNotifier (User user) {
        if (user == null) return;
        Notifier notifier = new NotifierGUI();
        user.setNotifier(notifier);

        // Configuración de notificaciones de conexión y desconexión de amigos
        notifier.setNotifyAddActiveFriend(friendName -> Platform.runLater(() -> {
            try {
                Runtime.getRuntime().exec(new String[] {"notify-send", friendName + " se ha conectado"});
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }

//            newActiveFriend(friendName);
        }));

        notifier.setNotifyRemoveActiveFriend(friendName -> Platform.runLater(() -> {
            try {
                Runtime.getRuntime().exec(new String[] {"notify-send", friendName + " se ha desconectado"});
            } catch (IOException exception) {
                System.err.println(exception.getMessage());
            }

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
//                  newActiveFriend(friendName);
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
//    private static void newActiveFriend(String friendName) {
//        friends.removeIf(hbox -> {
//            Label label = (Label) hbox.getChildren().getFirst();
//            return label.getText().equals(friendName);
//        });
//
//        Label nameLabel = new Label(friendName);
//        nameLabel.setStyle("-fx-font-size: 16px;");
//        nameLabel.setPrefWidth(90); // Ancho mínimo
//        Circle status = new Circle();
//        status.setRadius(11);
//        status.setFill(Color.GREEN);
//        Button removeButton = new Button("󰀒");
//        removeButton.setOnAction(e -> {
//            try {
//                user.removeFriendship(friendName);
//                friends.removeIf(hbox -> {
//                    Label label = (Label) hbox.getChildren().getFirst();
//                    return label.getText().equals(friendName);
//                });
//            } catch (Exception exception) {
//                System.err.println("Error al eliminar amigo: " + exception.getMessage());
//            }
//        });
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//
//        HBox friendBox = new HBox(10, nameLabel, spacer, status, removeButton);
//        friendBox.setAlignment(Pos.CENTER);
//        friends.addFirst(friendBox);
//    }


    public static void main(String[] args) {
        launch(args);
    }
}

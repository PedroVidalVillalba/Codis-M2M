package m2m.peer;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import m2m.peer.gui.NotifierGUI;

import java.io.IOException;
import java.util.Objects;

public class PeerMain extends Application {
    private static Stage primaryStage;
    private static User user;

    private static ObservableList<HBox> friends;
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
            activeFriends.add(friendName);

            Label nameLabel = new Label(friendName);
            nameLabel.setStyle("-fx-font-size: 16px;");
            nameLabel.setPrefWidth(90);
            Label statusLabel = new Label("🟢");
            Button removeButton = new Button("Eliminar");
            removeButton.setOnAction(e -> {
                try {
                    user.removeFriendship(friendName);
                } catch (Exception exception) {
                    System.err.println("Error al eliminar amigo: " + exception.getMessage());
                }
            });

            HBox friendBox = new HBox(10, nameLabel, statusLabel, removeButton);
            friends.add(friendBox);
        }));
        notifier.setNotifyRemoveActiveFriend(friendName -> Platform.runLater(() -> activeFriends.remove(friendName)));

    }

    public static void main(String[] args) {
        launch(args);
    }
}

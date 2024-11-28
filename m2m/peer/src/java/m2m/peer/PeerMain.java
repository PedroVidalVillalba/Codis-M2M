package m2m.peer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class PeerMain extends Application {
    private static Stage primaryStage;
    private static User user;

    public PeerMain() {}

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/Login.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage = stage;
        stage.setOnCloseRequest(event -> {
            if(user != null) {
                try {
                    user.logout();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
            System.exit(0);
        });

        stage.setTitle("Aplicación P2P segura");
        stage.setScene(scene);
        stage.show();
    }

    public static void setRoot(String fxml) {
        try {
            primaryStage.setScene(new Scene(FXMLLoader.load(Objects.requireNonNull(PeerMain.class.getResource(fxml)))));
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    public static void setUser(User user) {
        PeerMain.user = user;
    }

    public static User getUser() {
        return user;
    }

    public static void main(String[] args) {
        launch(args);
    }
}

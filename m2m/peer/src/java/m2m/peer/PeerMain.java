package m2m.peer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import m2m.shared.Security;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class PeerMain extends Application {
    private static Stage primaryStage;
    private static User user;

    public PeerMain() {}

    @Override
    public void start(Stage stage) throws Exception{

        FXMLLoader loader = new FXMLLoader(getClass().getResource("gui/Login.fxml"));
        Scene scene = new Scene(loader.load());

        primaryStage = stage;
        stage.setOnCloseRequest(event -> System.exit(0));

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

    public static void main(String[] args) throws Exception {
        /*
         * Poner pruebas a 1 para realizar pruebas, y a 0 para iniciar la gui
         * */
        int pruebas = 0;
        if (pruebas == 0) {
            launch(args);

        } else {
            User alice = new User("alice", "1234");
            User bob = new User("bob", "4321");
            System.out.println("Usuarios creados y conexión segura establecida con el servidor correctamente");

            try {
                alice.signUp();
                System.out.println("Alice registrada con éxito");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                alice.login();
                System.out.println("Alice inició sesión con éxito");
            }

            try {
                bob.signUp();
                System.out.println("Bob registrado con éxito");
            } catch (Exception e) {
                System.out.println(e.getMessage());
                bob.login();
                System.out.println("Bob inició sesión con éxito");
            }

            try {
                System.out.print("Alice busca usuarios \"bo\": ");
                System.out.println(alice.searchUsers("bo"));
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
            System.out.println("Alice busca usuarios \"bob\": " + alice.searchUsers("bob"));

            try {
                System.out.println("Solicitud de amistad de Alice a Bob");
                alice.requestFriendship("bob");
            } catch (Exception e) {
                System.out.println("Ya se había solicitado amistad");
            }

            try {
                System.out.println("Aceptación de amistad de Bob a Alice");
                bob.acceptFriendship("alice");
            } catch (Exception e) {
                System.out.println("No había ninguna solicitud de amistad pendiente entre Alice y Bob");
            }

            alice.greet("bob");
            System.out.println("Handshake entre Alice y Bob completo");

            alice.sendMessage("bob", "Hola Bob!");
            bob.sendMessage("alice", "¿Qué quieres?");

            /* Intento de suplantar a Alice */
            User malice = new User("malice", "666");
            Security security = malice.getSecurity();
            try {
                bob.getReference().message(alice.getReference(), security.encrypt("Hola Bob!", malice.getServer()));
            } catch (Exception e) {
                System.out.println("Malice no pudo falsificar el mensaje: " + e.getMessage());
            }

            System.out.println("Chat de Alice a Bob:");
            printChat(alice.getChat("bob"));

            System.out.println("Chat de Bob a Alice:");
            printChat(bob.getChat("alice"));

            System.out.println("\nBob se enfada con Alice porque no le responde y elimina su amistad");
            bob.removeFriendship("alice");
            System.out.print("Alice también elimina su amistad: ");
            try {
                alice.removeFriendship("bob");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            System.out.println("Bob se arrepiente y vuelve a enviarle una solicitud de amistad");
            bob.requestFriendship("alice");
            System.out.println("Pero Alice la rechaza");
            alice.rejectFriendship("bob");
            try{
                System.out.print("No se puede rechazar dos veces: ");
                alice.rejectFriendship("bob");
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }


            alice.logout();
            bob.logout();

            System.exit(0);
        }


    }

    private static void printChat(List<Message> messages) {
        for (Message message : messages) {
            switch (message.type()) {
                case SENT -> System.out.println("> " + message.message());
                case RECEIVED -> System.out.println("< " + message.message());
            }
        }
    }
}

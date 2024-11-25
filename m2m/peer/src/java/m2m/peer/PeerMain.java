package m2m.peer;

import m2m.shared.Security;

import java.util.List;

public class PeerMain {
    public static void main(String[] args) throws Exception {
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


        alice.logout();
        bob.logout();

        System.exit(0);
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

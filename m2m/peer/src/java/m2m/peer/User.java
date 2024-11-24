package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Security;
import m2m.shared.Security.Ephemeral;
import m2m.shared.Server;

import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/* TODO: añadir comprobaciones de todo */
/**
 * Clase con las funcionalidades principales que exporta la API para los usuarios.
 * Abstrae las llamadas a los procedimientos remotos en llamadas mucho más comprensibles
 * a ser invocadas desde la interfaz gráfica.
 * Gestiona también la seguridad.
 */
public class User {
    private final String username;
    private final String password;
    private final Peer reference;
    private final Security security;
    private final Server server;
    private final String authenticationCode;
    private final Map<String, Peer> activeFriends;    /* Pares (username, reference) */
    private final Map<String, List<Message>> chats;

    private record AuthenticatedServer(Server server, String serverKeyPath) {}

    public User(String username, String password) throws Exception {
        this.security = new Security();
        this.activeFriends = new HashMap<>();
        Map<String, SecretKey> authenticationKeys = new HashMap<>();
        this.chats = new HashMap<>();
        AuthenticatedServer authenticatedServer = findServer();
        if (authenticatedServer == null) {
            throw new RemoteException("No se pudo encontrar ningún servidor conocido");
        }
        this.server = authenticatedServer.server();
        PublicKey serverPublicKey = security.loadPublicKey(authenticatedServer.serverKeyPath());
        this.username = username;
        this.password = security.digest(password, username);
        this.reference = new SecurePeer(username, security, activeFriends, authenticationKeys, chats, server, serverPublicKey);

        Ephemeral ephemeral = security.generateEphemeral(server);
        server.greet(reference, ephemeral.publicKey(), ephemeral.nonce());
        this.authenticationCode = security.encrypt(Server.AUTHENTICATION_STRING, server);
    }

    /* Getters */
    public String getUsername() {
        return username;
    }

    public Peer getReference() {
        return reference;
    }

    public Security getSecurity() {
        return security;
    }

    public Server getServer() {
        return server;
    }

    public Map<String, Peer> getActiveFriends() {
        return activeFriends;
    }

    public Map<String, List<Message>> getChats() {
        return chats;
    }

    /* Métodos públicos que exportan la funcionalidad de la API */
    public void greet(String friendName) throws Exception {
        Peer friend = activeFriends.get(friendName);
        Ephemeral ephemeral = security.generateEphemeral(friend);
        friend.greet(reference, ephemeral.publicKey(), ephemeral.nonce());
    }

    public void sendMessage(String friendName, String message) throws Exception {
        Peer friend = activeFriends.get(friendName);
        List<Message> chat = chats.get(friendName);
        chat.add(new Message(message, MessageType.SENT));
        friend.message(this.reference, security.encrypt(message, friend));
    }

    public void signUp() throws Exception {
        server.signUp(this.reference, this.password, authenticationCode);
    }
    public void login() throws Exception {
        server.login(this.reference, this.password, authenticationCode);
    }
    public void logout() throws Exception {
        server.logout(this.reference, authenticationCode);
    }

    public void requestFriendship(String friend) throws Exception {
        server.friendRequest(this.reference, friend, authenticationCode);
    }

    public void acceptFriendship(String friend) throws Exception {
        server.friendAccept(this.reference, friend, authenticationCode);
    }

    public void rejectFriendship(String friend) throws Exception {
        server.friendReject(this.reference, friend, authenticationCode);
    }

    /* Métodos privados que facilitan la lógica del código */
    private static AuthenticatedServer findServer() {
        /* TODO: rellenar este stub */
        return null;
    }
}

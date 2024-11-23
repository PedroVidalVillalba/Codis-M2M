package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Server;

import java.rmi.RemoteException;
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
    private String username;
    private String password;
    private Peer reference;
    private PeerSecurity security;
    private Server server;
    private Map<String, Peer> activeFriends;    /* Pares (username, reference) */
    private Map<String, List<Message>> chats;

    public User(String username, String password) throws RemoteException {
        this.security = new PeerSecurity();
        this.activeFriends = new HashMap<>();
        this.chats = new HashMap<>();
        this.server = findServer();
        if (server == null) {
            throw new RemoteException("No se pudo encontrar ningún servidor conocido");
        }
        this.username = username;
        this.password = security.digest(password, username);
        this.reference = new SecurePeer(username, security, activeFriends, chats, server);

        server.greet(reference);
    }

    /* Getters */
    public String getUsername() {
        return username;
    }

    public Peer getReference() {
        return reference;
    }

    public PeerSecurity getSecurity() {
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
    public boolean sendMessage(String friendName, String message) throws RemoteException {
        Peer friend = activeFriends.get(friendName);
        List<Message> chat = chats.get(friendName);
        chat.add(new Message(message, MessageType.SENT));
        return friend.message(this.reference, security.encrypt(message, friend));
    }

    public boolean receiveMessage(String friendName, String message) {
        return true;
    }

    public boolean signUp() {
        return server.signUp(this.reference, this.username, this.password, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }
    public boolean login() {
        return server.login(this.reference, this.username, this.password, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }
    public boolean logout() {
        return server.logout(this.reference, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean requestFriendship(String friend) {
        return server.friendRequest(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean acceptFriendship(String friend) {
        return server.friendAccept(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean rejectFriendship(String friend) {
        return server.friendReject(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    /* Métodos privados que facilitan la lógica del código */
    private static Server findServer() {
        /* TODO: rellenar este stub */
        return null;
    }
}

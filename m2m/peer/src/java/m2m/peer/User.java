package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Security;
import m2m.shared.Server;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.KeyPair;
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
    private Security security;
    private Server server;
    private Map<String, Peer> activeFriends;    /* Pares (username, reference) */
    private Map<String, List<Message>> chats;

    public User(String username, String password) throws Exception {
        this.security = new Security();
        this.activeFriends = new HashMap<>();
        this.chats = new HashMap<>();
        this.server = findServer();
//        if (server == null) {
//            throw new RemoteException("No se pudo encontrar ningún servidor conocido");
//        }
        this.username = username;
        this.password = security.digest(password, username);
        this.reference = new SecurePeer(username, security, activeFriends, chats, server);

//        if (!greetServer()) throw new RemoteException("No se pudo establecer una conexión segura con el servidor");
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
    public boolean greet(Peer peer) throws Exception {
       KeyPair keyPair = security.generateKeyPair(peer);
       return peer.greet(reference, keyPair.getPublic());
    }

    public boolean sendMessage(String friendName, String message) throws Exception {
        Peer friend = activeFriends.get(friendName);
        List<Message> chat = chats.get(friendName);
        chat.add(new Message(message, MessageType.SENT));
        return friend.message(this.reference, security.encrypt(message, friend));
    }

    public boolean receiveMessage(String friendName, String message) {
        return true;
    }

    public boolean signUp() throws Exception {
        return server.signUp(this.reference, this.username, this.password, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }
    public boolean login() throws Exception {
        return server.login(this.reference, this.username, this.password, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }
    public boolean logout() throws Exception {
        return server.logout(this.reference, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean requestFriendship(String friend) throws Exception {
        return server.friendRequest(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean acceptFriendship(String friend) throws Exception {
        return server.friendAccept(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    public boolean rejectFriendship(String friend) throws Exception {
        return server.friendReject(this.reference, friend, security.encrypt(Server.AUTHENTICATION_STRING, server));
    }

    /* Métodos privados que facilitan la lógica del código */
    private static Server findServer() {
        /* TODO: rellenar este stub */
        return null;
    }

    private boolean greetServer() throws Exception {
        KeyPair keyPair = security.generateKeyPair(server);
        return server.greet(reference, keyPair.getPublic());
    }

}

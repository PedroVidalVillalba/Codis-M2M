package m2m.server;

import m2m.shared.Peer;
import m2m.shared.Server;
import m2m.shared.Security;

import javax.crypto.SecretKey;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// TODO: tema seguridad y conexión RMI
public class SecureServer extends UnicastRemoteObject implements Server{

    private static DataBase database;
    private Map<String, Peer> connectedUsers;
    private Security security;

    public SecureServer() throws RemoteException{
        super();
        database = DataBase.getCurrentDB();
        this.connectedUsers = new HashMap<>();
        this.security = new Security();
    }

    @Override
    public boolean greet(Peer client, PublicKey clientPublicKey) throws Exception {
        /* Generar una clave efímera */
        KeyPair keyPair = security.generateKeyPair();

        /* Calcular el secreto compartido usando la clave pública de quien inicia el saludo */
        byte[] sharedSecret = security.computeSharedSecret(keyPair.getPrivate(), clientPublicKey);

        boolean success = client.greetBack(this, keyPair.getPublic());
        if (!success) throw new RemoteException("Fallo en el saludo con " + client.getUsername());

        SecretKey secretKey = security.deriveAESKey(sharedSecret, new byte[0]);
        security.addSecretKey(client, secretKey);

        return true;
    }

    @Override
    public boolean signUp(Peer peer, String username, String password, String authentication) throws Exception {
        if (peer == null || username == null || password == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;

        boolean success = database.registerUser(username, password);
        if (!success) return false;

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
        return true;
    }

    @Override
    public boolean login(Peer peer, String username, String password, String authentication) throws Exception {
        if (peer == null || username == null || password == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;

        boolean success = database.loginUser(username, password);
        if (!success) return false;

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
        return true;
    }

    @Override
    public boolean logout(Peer peer, String authentication) throws Exception {
        if (peer == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;

        notifyFriendDisconnection(peer.getUsername());
        connectedUsers.remove(peer.getUsername());
        return true;
    }

    @Override
    public boolean friendRequest(Peer peer, String username, String authentication) throws Exception {
        if (peer == null || username == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;
        return database.friendRequest(peer.getUsername(), username);
    }

    @Override
    public boolean friendAccept(Peer peer, String username, String authentication) throws Exception{
        if (peer == null || username == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;
        return database.friendAccept(username, peer.getUsername());
    }

    @Override
    public boolean friendReject(Peer peer, String username, String authentication) throws Exception{
        if (peer == null || username == null || authentication == null || peerFailedToAuthenticate(authentication, peer)) return false;
        return database.friendReject(username, peer.getUsername());
    }

    private boolean peerFailedToAuthenticate(String authentication, Peer peer) throws Exception {
        return !security.decrypt(authentication, peer).equals(Server.AUTHENTICATION_STRING);
    }

    // Avisa a todos los amigos conectados de un usuario de que se acaba de conectar
    private void notifyFriendConnection(String username) throws Exception {
        HashMap<String, Peer> mapFriends = new HashMap<>();
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        for (String friend : friends) {
            Peer peer = connectedUsers.get(friend);
            peer.addActiveFriend(connectedUsers.get(username), security.encrypt(AUTHENTICATION_STRING, peer));
            mapFriends.put(friend, peer);
        }

        // Avisa también al usuario recién conectado
        Peer connecting = connectedUsers.get(username);
        connecting.addActiveFriend(mapFriends, security.encrypt(AUTHENTICATION_STRING, connecting));
    }

    // Avisa a todos los amigos conectados de que el usuario se acaba de desconectar
    private void notifyFriendDisconnection(String username) throws Exception {
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        for (String friend : friends) {
            Peer peer = connectedUsers.get(friend);
            peer.removeActiveFriend(connectedUsers.get(username), security.encrypt(AUTHENTICATION_STRING, peer));
        }
    }
}

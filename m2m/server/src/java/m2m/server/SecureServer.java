package m2m.server;

import m2m.shared.Peer;
import m2m.shared.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;

// TODO: tema seguridad y conexión RMI
public class SecureServer extends UnicastRemoteObject implements Server{

    private static DataBase database;
    private HashMap<String, Peer> connectedUsers;

    public SecureServer() throws RemoteException{
        super();
        database = DataBase.getCurrentDB();
        connectedUsers = new HashMap<>();
    }

    @Override
    public boolean greet(Peer peer) throws RemoteException {
        return false;
    }

    @Override
    public boolean signUp(Peer peer, String username, String password, String authentication) throws RemoteException {
        if(peer == null || username == null || password == null || authentication == null) return false;
        // Comprobar autenticidad del usuario

        boolean result = database.registerUser(username, password);
        if (!result) {
            return false;
        } else {
            connectedUsers.put(username, peer);
            notifyFriendConnection(username);
            return true;
        }
    }

    @Override
    public boolean login(Peer peer, String username, String password, String authentication) throws RemoteException {
        if(peer == null || username == null || password == null || authentication == null) return false;
        // Comprobar autenticidad del usuario

        boolean result = database.loginUser(username, password);
        if (!result) {
            return false;
        } else {
            connectedUsers.put(username, peer);
            notifyFriendConnection(username);
            return true;
        }
    }

    @Override
    public boolean logout(Peer peer, String authentication) throws RemoteException {
        if(peer == null || authentication == null) return false;
        // Comprobar autenticidad del usuario

        notifyFriendDisconnection(peer.getUsername());
        connectedUsers.remove(peer.getUsername());
        return true;

    }

    @Override
    public boolean friendRequest(Peer peer, String username, String authentication) throws RemoteException {
        if(peer == null || username == null || authentication == null) return false;
        return database.friendRequest(peer.getUsername(), username);
    }

    @Override
    public boolean friendAccept(Peer peer, String username, String authentication) throws RemoteException{
        if(peer == null || username == null || authentication == null) return false;
        return database.friendAccept(username, peer.getUsername());
    }

    @Override
    public boolean friendReject(Peer peer, String username, String authentication) throws RemoteException{
        if(peer == null || username == null || authentication == null) return false;
        return database.friendReject(username, peer.getUsername());
    }

    private boolean peerFailedToAuthenticate(String authentication, Peer peer) throws RemoteException {
        // TODO: clase de seguridad, ¿similar a la de peer o la misma?
        //return !security.decrypt(authentication, peer).equals(Server.AUTHENTICATION_STRING);
        return true;
    }

    // Avisa a todos los amigos conectados de un usuario de que se acaba de conectar
    private void notifyFriendConnection(String username) throws RemoteException {
        HashMap<String, Peer> mapFriends = new HashMap<>();
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        for(String friend : friends) {
            Peer peer = connectedUsers.get(friend);
            peer.addActiveFriend(connectedUsers.get(username), AUTHENTICATION_STRING);
            mapFriends.put(friend, peer);
        }

        // Avisa también al usuario recién conectado
        connectedUsers.get(username).addActiveFriend(mapFriends, AUTHENTICATION_STRING);
    }

    // Avisa a todos los amigos conectados de que el usuario se acaba de desconectar
    private void notifyFriendDisconnection(String username) throws RemoteException {
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        for(String friend : friends) {
            Peer peer = connectedUsers.get(friend);
            peer.removeActiveFriend(connectedUsers.get(username), AUTHENTICATION_STRING);
        }
    }

    public static void main(String[] args) throws RemoteException{
        System.out.println(System.getProperty("user.dir"));
        SecureServer prueba = new SecureServer();

        System.out.println(database.getUsers());
        System.out.println(database.getFriends("bob"));


        DataBase.closeCurrentDB();

    }
}

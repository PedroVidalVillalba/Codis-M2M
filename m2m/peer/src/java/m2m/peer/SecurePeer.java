package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Security;
import m2m.shared.Server;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecurePeer extends UnicastRemoteObject implements Peer {
    final private String username;
    private Security security;
    private Map<String, Peer> activeFriends;
    private Map<String, List<Message>> chats;
    private Server server;

    public SecurePeer(String username, Security security, Map<String, Peer> activeFriends, Map<String, List<Message>> chats, Server server) throws RemoteException {
        super();
        this.username = username;
        this.security = security;
        this.activeFriends = activeFriends;
        this.chats = chats;
        this.server = server;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public boolean greet(Peer greeter, PublicKey greeterPublicKey) throws Exception {
        /* Generar una clave efímera */
        KeyPair keyPair = security.generateKeyPair();

        /* Calcular el secreto compartido usando la clave pública de quien inicia el saludo */
        byte[] sharedSecret = security.computeSharedSecret(keyPair.getPrivate(), greeterPublicKey);

        boolean success = greeter.greetBack(this, keyPair.getPublic());
        if (!success) throw new RemoteException("Fallo en el saludo con " + greeter.getUsername());

        SecretKey secretKey = security.deriveAESKey(sharedSecret, new byte[0]);
        security.addSecretKey(greeter, secretKey);

        return true;
    }

    @Override
    public boolean greetBack(Remote greeted, PublicKey greetedPublicKey) throws Exception {
        /* Generar una clave efímera */
        KeyPair keyPair = security.getOngoingKeyPair(greeted);

        /* Calcular el secreto compartido usando la clave pública de quien responde al saludo */
        byte[] sharedSecret = security.computeSharedSecret(keyPair.getPrivate(), greetedPublicKey);

        security.removeOngoingKeyPair(greeted);

        SecretKey secretKey = security.deriveAESKey(sharedSecret, new byte[0]);
        security.addSecretKey(greeted, secretKey);

        return true;
    }

    @Override
    public boolean message(Peer sender, String message) throws Exception {
        Peer friend = activeFriends.get(sender.getUsername());
        if (friend == null) return false;
        security.decrypt(message, friend);
        List<Message> chat = chats.get(friend.getUsername());
        chat.add(new Message(message, MessageType.RECEIVED));
        return true;
    }

    @Override
    public boolean addActiveFriend(Peer friend, String authentication) throws Exception {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.put(friend.getUsername(), friend);
        chats.put(friend.getUsername(), new ArrayList<>());
        return true;
    }

    @Override
    public boolean addActiveFriend(Map<String, Peer> friends, String authentication) throws Exception {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.putAll(friends);
        for (String friend : activeFriends.keySet()) {
            chats.put(friend, new ArrayList<>());
        }
        return true;
    }

    @Override
    public boolean removeActiveFriend(Peer friend, String authentication) throws Exception {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.remove(friend.getUsername());
        chats.remove(friend.getUsername());
        return true;
    }

    private boolean serverFailedToAuthenticate(String authentication) throws Exception {
        return !security.decrypt(authentication, server).equals(Server.AUTHENTICATION_STRING);
    }
}

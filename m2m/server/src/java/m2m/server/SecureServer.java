package m2m.server;

import m2m.shared.Peer;
import m2m.shared.Server;
import m2m.shared.Security;
import m2m.shared.Security.Ephemeral;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

// TODO: tema seguridad y conexión RMI; hacer que sea la propia base de datos la que lance las excepciones
public class SecureServer extends UnicastRemoteObject implements Server{

    private static DataBase database;
    private final Map<String, Peer> connectedUsers;
    private final Security security;
    private final PrivateKey privateKey;

    public SecureServer() throws Exception {
        super();
        database = DataBase.getCurrentDB();
        this.connectedUsers = new HashMap<>();
        this.security = new Security();
        String privateKeyPath = "/keys/server_private_key_" + InetAddress.getLocalHost().getHostName() + ".pem";
        this.privateKey = security.loadPrivateKey(privateKeyPath);
    }

    @Override
    public void greet(Peer client, PublicKey clientPublicKey, byte[] clientNonce) throws Exception {
        /* Validar entradas */
        Security.ensureNotNull(client, clientPublicKey, clientNonce);

        /* Generar una clave efímera y un número aleatorio de un solo uso */
        Ephemeral ephemeral = security.generateEphemeral();
        PrivateKey ephemeralPrivateKey = ephemeral.privateKey();
        PublicKey ephemeralPublicKey = ephemeral.publicKey();
        byte[] serverNonce = ephemeral.nonce();

        /* Calcular el secreto compartido usando la clave pública de quien inicia el saludo */
        byte[] sharedSecret = security.computeSharedSecret(ephemeralPrivateKey, clientPublicKey);
        /* Combinar la información pública usada para verificar que el saludo fue correcto */
        byte[] handshakeData = security.combine(clientNonce, serverNonce, clientPublicKey.getEncoded(), ephemeralPublicKey.getEncoded());

        /* Cifrar la información del saludo con la clave privada del servidor para autenticarse  */
        byte[] signature = security.sign(handshakeData, privateKey);
        /* Generar la clave compartida para la encriptación de la comunicación */
        SecretKey secretKey = security.deriveSecretKey(sharedSecret, handshakeData);

        /* Enviar la clave pública del servidor, el nonce utilizado y la autenticación cifrada, lo que también vale para confirmar que el servidor tiene la clave */
        byte[] clientResponse = client.greetBack(this, ephemeralPublicKey, serverNonce, security.encrypt(signature, secretKey));
        if (!Arrays.equals(security.digest(handshakeData, signature), security.decrypt(clientResponse, secretKey))) {
            throw new GeneralSecurityException("Falló la verificación de la respuesta del cliente durante el saludo.");
        }

        security.storeSecretKey(client, secretKey);
    }

    @Override
    public void signUp(Peer peer, String password, String authentication) throws Exception {
        Security.ensureNotNull(peer, password, authentication);
        verifyAuthentication(authentication, peer);

        String username = peer.getUsername();
        if (!database.registerUser(username, password)) {
            throw new SQLException("No se pudo registrar al usuario en la base de datos");
        }

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
    }

    @Override
    public void login(Peer peer, String password, String authentication) throws Exception {
        Security.ensureNotNull(peer, password, authentication);
        verifyAuthentication(authentication, peer);

        String username = peer.getUsername();
        if (!database.loginUser(username, password)) {
            throw new SQLException("No se pudo iniciar sesión con el usuario en la base de datos");
        }

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
    }

    @Override
    public void logout(Peer peer, String authentication) throws Exception {
        Security.ensureNotNull(peer, authentication);
        verifyAuthentication(authentication, peer);

        notifyFriendDisconnection(peer.getUsername());
        connectedUsers.remove(peer.getUsername());
    }

    @Override
    public void friendRequest(Peer peer, String username, String authentication) throws Exception {
        Security.ensureNotNull(peer, username, authentication);
        verifyAuthentication(authentication, peer);

        database.friendRequest(peer.getUsername(), username);
    }

    @Override
    public void friendAccept(Peer peer, String username, String authentication) throws Exception{
        Security.ensureNotNull(peer, username, authentication);
        verifyAuthentication(authentication, peer);

        database.friendAccept(username, peer.getUsername());
    }

    @Override
    public void friendReject(Peer peer, String username, String authentication) throws Exception{
        Security.ensureNotNull(peer, username, authentication);
        verifyAuthentication(authentication, peer);

        database.friendReject(username, peer.getUsername());
    }

    private void verifyAuthentication(String authentication, Peer peer) throws Exception {
        if (!security.decrypt(authentication, peer).equals(AUTHENTICATION_STRING)) {
            throw new GeneralSecurityException("No se pudo garantizar la seguridad de la comunicación con el usuario " + peer.getUsername());
        }
    }

    private String authenticationCode(Peer client) throws Exception {
        return security.encrypt(AUTHENTICATION_STRING, client);
    }

    // Avisa a todos los amigos conectados de un usuario de que se acaba de conectar
    private void notifyFriendConnection(String username) throws Exception {
        HashMap<String, Peer> activeFriends = new HashMap<>();
        HashMap<String, SecretKey> encryptedAuthenticationKeys = new HashMap<>();
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        Peer user = connectedUsers.get(username);
        for (String friendName : friends) {
            Peer friend = connectedUsers.get(friendName);

            SecretKey authenticationKey = security.generateAuthenticationKey();
            SecretKey encryptedKeyForFriend = security.encrypt(authenticationKey, friend);
            SecretKey encryptedKeyForUser = security.encrypt(authenticationKey, user);

            friend.addActiveFriend(user, encryptedKeyForFriend, authenticationCode(friend));
            activeFriends.put(friendName, friend);
            encryptedAuthenticationKeys.put(friendName, encryptedKeyForUser);
        }

        // Avisa también al usuario recién conectado
        user.addActiveFriend(activeFriends, encryptedAuthenticationKeys, authenticationCode(user));
    }

    // Avisa a todos los amigos conectados de que el usuario se acaba de desconectar
    private void notifyFriendDisconnection(String username) throws Exception {
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        for (String friend : friends) {
            Peer peer = connectedUsers.get(friend);
            peer.removeActiveFriend(connectedUsers.get(username), authenticationCode(peer));
        }
    }
}

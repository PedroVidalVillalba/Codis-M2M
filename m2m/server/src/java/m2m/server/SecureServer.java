package m2m.server;

import m2m.shared.Peer;
import m2m.shared.Server;
import m2m.shared.security.Security;
import m2m.shared.security.Security.Ephemeral;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class SecureServer extends UnicastRemoteObject implements Server{
    private final DataBase database;
    private final Map<String, Peer> connectedUsers;
    private final Security security;
    private final PrivateKey privateKey;

    public SecureServer() throws Exception {
        super();
        database = DataBase.getCurrentDB();
        this.security = new Security();
        this.connectedUsers = new HashMap<>();
        String privateKeyPath = "/keys/server_private_key_" + InetAddress.getLocalHost().getHostName() + ".pem";
        this.privateKey = Security.loadPrivateKey(privateKeyPath);
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
        byte[] handshakeData = Security.combine(clientNonce, serverNonce, clientPublicKey.getEncoded(), ephemeralPublicKey.getEncoded());

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
    public void signUp(Peer peer, String password, byte[] authentication) throws Exception {
        Security.ensureNotNull(peer, password, authentication);
        verifyAuthentication(authentication, Method.SIGN_UP, peer, password);

        String username = peer.getUsername();
        byte[] rawPassword = Base64.getDecoder().decode(password);
        database.registerUser(username, rawPassword);

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
    }

    @Override
    public void login(Peer peer, String password, byte[] authentication) throws Exception {
        Security.ensureNotNull(peer, password, authentication);
        verifyAuthentication(authentication, Method.LOGIN, peer, password);

        String username = peer.getUsername();
        if (connectedUsers.containsKey(username)) {
            throw new Exception("El usuario ya ha iniciado sesión desde otro lugar");
        }
        byte[] rawPassword = Base64.getDecoder().decode(security.decrypt(password, peer));
        database.loginUser(username, rawPassword);

        connectedUsers.put(username, peer);
        notifyFriendConnection(username);
    }

    @Override
    public void logout(Peer peer, byte[] authentication) throws Exception {
        Security.ensureNotNull(peer, authentication);
        verifyAuthentication(authentication, Method.LOGOUT, peer);

        notifyFriendDisconnection(peer.getUsername());
        connectedUsers.remove(peer.getUsername());
    }

    @Override
    public void friendRequest(Peer user, String friendName, byte[] authentication) throws Exception {
        Security.ensureNotNull(user, friendName, authentication);
        verifyAuthentication(authentication, Method.FRIEND_REQUEST, user, friendName);

        database.friendRequest(user.getUsername(), friendName);
    }

    @Override
    public void friendAccept(Peer user, String friendName, byte[] authentication) throws Exception {
        Security.ensureNotNull(user, friendName, authentication);
        verifyAuthentication(authentication, Method.FRIEND_ACCEPT, user, friendName);

        database.friendAccept(friendName, user.getUsername());

        // Si el nuevo amigo está conectado, se notifica a ambos que están conectados
        Peer friend = connectedUsers.get(friendName);
        if (friend != null) {
            SecretKey authenticationKey = security.generateAuthenticationKey();
            SecretKey encryptedKeyForFriend = security.encrypt(authenticationKey, friend);
            SecretKey encryptedKeyForUser = security.encrypt(authenticationKey, user);

            friend.addActiveFriend(user, encryptedKeyForFriend, authenticate(Peer.Method.ADD_ACTIVE_FRIEND, friend, user, encryptedKeyForFriend));
            user.addActiveFriend(friend, encryptedKeyForUser, authenticate(Peer.Method.ADD_ACTIVE_FRIEND, user, friend, encryptedKeyForUser));
        }
    }

    @Override
    public void friendReject(Peer user, String friendName, byte[] authentication) throws Exception {
        Security.ensureNotNull(user, friendName, authentication);
        verifyAuthentication(authentication, Method.FRIEND_REJECT, user, friendName);

        database.friendReject(friendName, user.getUsername());
    }

    @Override
    public void friendRemove(Peer user, String friendName, byte[] authentication) throws Exception {
        Security.ensureNotNull(user, friendName, authentication);
        verifyAuthentication(authentication, Method.FRIEND_REMOVE, user, friendName);

        database.friendRemove(user.getUsername(), friendName);

        // Si el amigo está conectado, se le quita de la lista de amigos activos
        Peer friend = connectedUsers.get(friendName);
        if (friend != null) {
            friend.removeActiveFriend(user, authenticate(Peer.Method.REMOVE_ACTIVE_FRIEND, friend, user));
            user.removeActiveFriend(friend, authenticate(Peer.Method.REMOVE_ACTIVE_FRIEND, user, friend));
        }
    }

    @Override
    public List<String> searchUsers(Peer peer, String pattern, byte[] authentication) throws Exception {
        Security.ensureNotNull(peer, pattern, authentication);
        verifyAuthentication(authentication, Method.SEARCH_USERS, peer, pattern);

        return database.searchUsers(pattern);
    }

    private void verifyAuthentication(byte[] authentication, Server.Method method, Peer client, Object... parameters) throws Exception {
        if (method.requiresLogin() && !connectedUsers.containsValue(client)) {
            throw new GeneralSecurityException("Se requiere autenticación del usuario " + client.getUsername() + " para ejecutar el método " + method);
        }

        byte[] nonce = security.extractNonce(authentication);
        byte[] encryptedAuthentication = security.removeNonce(authentication);

        byte[] serializedData = Security.serialize(method, parameters);
        byte[] expectedAuthentication = security.digest(serializedData, nonce);
        byte[] decryptedAuthentication = security.decrypt(encryptedAuthentication, client);

        if (!Arrays.equals(expectedAuthentication, decryptedAuthentication)) {
            throw new GeneralSecurityException("Fallo de autenticación en el método " + method + " con el usuario " + client.getUsername());
        }
    }

    private byte[] authenticate(Peer.Method method, Peer client, Object... parameters) throws Exception {
        byte[] nonce = security.generateNonce();
        byte[] serializedData = Security.serialize(method, parameters);
        byte[] hashedAuthentication = security.digest(serializedData, nonce);
        byte[] authenticationCode = security.encrypt(hashedAuthentication, client);
        return Security.combine(nonce, authenticationCode);
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

            friend.addActiveFriend(user, encryptedKeyForFriend, authenticate(Peer.Method.ADD_ACTIVE_FRIEND, friend, user, encryptedKeyForFriend));
            activeFriends.put(friendName, friend);
            encryptedAuthenticationKeys.put(friendName, encryptedKeyForUser);
        }

        // Avisa también al usuario recién conectado
        user.addActiveFriend(activeFriends, encryptedAuthenticationKeys, authenticate(Peer.Method.ADD_ACTIVE_FRIEND, user, activeFriends, encryptedAuthenticationKeys));
    }

    // Avisa a todos los amigos conectados de que el usuario se acaba de desconectar
    private void notifyFriendDisconnection(String username) throws Exception {
        ArrayList<String> friends = database.getFriends(username);
        // Se filtra por los usuarios conectados actualmente
        friends.retainAll(connectedUsers.keySet());

        Peer user = connectedUsers.get(username);
        for (String friendName : friends) {
            Peer friend = connectedUsers.get(friendName);
            friend.removeActiveFriend(user, authenticate(Peer.Method.REMOVE_ACTIVE_FRIEND, friend, user));
        }
    }
}

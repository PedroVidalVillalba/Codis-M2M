package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Security;
import m2m.shared.Security.Ephemeral;
import m2m.shared.Server;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.*;

public class SecurePeer extends UnicastRemoteObject implements Peer {
    private final String username;
    private final Security security;
    private final Map<String, Peer> activeFriends;
    private final Map<String, SecretKey> authenticationKeys;
    private final Map<String, List<Message>> chats;
    private final Server server;
    private final PublicKey serverPublicKey;

    public SecurePeer(String username, Security security, Map<String, Peer> activeFriends, Map<String, SecretKey> authenticationKeys, Map<String, List<Message>> chats, Server server, PublicKey serverPublicKey) throws RemoteException {
        super();
        Security.ensureNotNull(username, security, activeFriends, authenticationKeys, server, serverPublicKey);
        this.username = username;
        this.security = security;
        this.activeFriends = activeFriends;
        this.authenticationKeys = authenticationKeys;
        this.chats = chats;
        this.server = server;
        this.serverPublicKey = serverPublicKey;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public void greet(Peer greeter, PublicKey greeterPublicKey, byte[] greeterNonce) throws Exception {
        /* Validar entradas */
        Security.ensureNotNull(greeter, greeterPublicKey, greeterNonce);

        /* Generar una clave efímera y un número aleatorio de un solo uso */
        Ephemeral ephemeral = security.generateEphemeral();
        PrivateKey ephemeralPrivateKey = ephemeral.privateKey();
        PublicKey ephemeralPublicKey = ephemeral.publicKey();
        byte[] nonce = ephemeral.nonce();

        /* Calcular el secreto compartido usando la clave pública de quien inicia el saludo */
        byte[] sharedSecret = security.computeSharedSecret(ephemeralPrivateKey, greeterPublicKey);
        /* Combinar la información pública usada para verificar que el saludo fue correcto */
        byte[] handshakeData = Security.combine(greeterNonce, nonce, greeterPublicKey.getEncoded(), ephemeralPublicKey.getEncoded());

        /* Cifrar la información del saludo con la clave compartida por el servidor para autenticarse */
        SecretKey authenticationKey = authenticationKeys.get(greeter.getUsername());
        byte[] challenge = security.digest(handshakeData, Security.combine(greeter.getUsername().getBytes(), username.getBytes()));
        byte[] signature = security.encrypt(challenge, authenticationKey);

        /* Generar la clave compartida para la encriptación de la comunicación */
        SecretKey secretKey = security.deriveSecretKey(sharedSecret, handshakeData);

        /* Enviar la clave pública del servidor, el nonce utilizado y la autenticación cifrada, lo que también vale para confirmar que el servidor tiene la clave */
        byte[] greeterResponse = greeter.greetBack(this, ephemeralPublicKey, nonce, security.encrypt(signature, secretKey));
        if (!Arrays.equals(security.digest(handshakeData, signature), security.decrypt(greeterResponse, authenticationKey))) {
            throw new GeneralSecurityException("Falló la verificación de la respuesta del peer durante el saludo.");
        }

        security.storeSecretKey(greeter, secretKey);
    }

    @Override
    public byte[] greetBack(Remote greeted, PublicKey greetedPublicKey, byte[] greetedNonce, byte[] challenge) throws Exception {
        /* Validar entradas */
        Security.ensureNotNull(greeted, greetedPublicKey, greetedNonce, challenge);

        /* Recuperar la información generada previamente asociada a este saludo */
        Ephemeral ephemeral = security.getEphemeral(greeted);
        PrivateKey privateKey = ephemeral.privateKey();
        PublicKey publicKey = ephemeral.publicKey();
        byte[] nonce = ephemeral.nonce();

        /* Calcular el secreto compartido usando la clave pública de quien responde al saludo */
        byte[] sharedSecret = security.computeSharedSecret(privateKey, greetedPublicKey);
        /* Combinar la información pública usada para verificar que el saludo fue correcto */
        byte[] handshakeData = Security.combine(nonce, greetedNonce, publicKey.getEncoded(), greetedPublicKey.getEncoded());

        /* Generar la clave compartida para la encriptación de la comunicación */
        SecretKey secretKey = security.deriveSecretKey(sharedSecret, handshakeData);

        /* Autenticar la identidad del iniciador de la conversación, así como confirmar que compartimos la misma clave secreta */
        byte[] signature = security.decrypt(challenge, secretKey);
        authenticateRemote(greeted, handshakeData, signature);

        security.storeSecretKey(greeted, secretKey);

        return generateResponse(greeted, handshakeData, signature, secretKey);
    }

    @Override
    public void message(Peer sender, String message) throws Exception {
        Security.ensureNotNull(sender, message);

        Peer friend = activeFriends.get(sender.getUsername());
        if (friend == null) {
            throw new RuntimeException("El emisor del mensaje no está registrado como amigo del receptor");
        }

        String decryptedMessage = security.decrypt(message, friend);
        List<Message> chat = chats.get(friend.getUsername());
        chat.add(new Message(decryptedMessage, MessageType.RECEIVED));
    }

    @Override
    public void addActiveFriend(Peer friend, SecretKey encryptedAuthenticationKey, byte[] authentication) throws Exception {
        Security.ensureNotNull(friend, encryptedAuthenticationKey, authentication);
        verifyServerAuthentication(authentication, Method.ADD_ACTIVE_FRIEND, friend, encryptedAuthenticationKey);

        String friendName = friend.getUsername();
        SecretKey authenticationKey = security.decrypt(encryptedAuthenticationKey, server);
        activeFriends.put(friendName, friend);
        authenticationKeys.put(friendName, authenticationKey);
        chats.put(friendName, new ArrayList<>());
    }

    @Override
    public void addActiveFriend(Map<String, Peer> friends, Map<String, SecretKey> encryptedAuthenticationKeys, byte[] authentication) throws Exception {
        Security.ensureNotNull(friends, encryptedAuthenticationKeys, authentication);
        verifyServerAuthentication(authentication, Method.ADD_ACTIVE_FRIEND, friends, encryptedAuthenticationKeys);

        activeFriends.putAll(friends);

        for (String friendName : activeFriends.keySet()) {
            SecretKey authenticationKey = security.decrypt(encryptedAuthenticationKeys.get(friendName), server);
            authenticationKeys.put(friendName, authenticationKey);
            chats.put(friendName, new ArrayList<>());
        }
    }

    @Override
    public void removeActiveFriend(Peer friend, byte[] authentication) throws Exception {
        Security.ensureNotNull(friend, authentication);
        verifyServerAuthentication(authentication, Method.REMOVE_ACTIVE_FRIEND, friend);

        String friendName = friend.getUsername();
        activeFriends.remove(friendName);
        authenticationKeys.remove(friendName);
        chats.remove(friendName);
        security.removeSecretKey(friend);
    }

    private void verifyServerAuthentication(byte[] authentication, Peer.Method method, Object... parameters) throws Exception {
        byte[] nonce = security.extractNonce(authentication);
        byte[] encryptedAuthentication = security.removeNonce(authentication);

        byte[] serializedData = Security.serialize(method, parameters);
        byte[] expectedAuthentication = security.digest(serializedData, nonce);
        byte[] decryptedAuthentication = security.decrypt(encryptedAuthentication, server);

        if (!Arrays.equals(expectedAuthentication, decryptedAuthentication)) {
            throw new GeneralSecurityException("Fallo de autenticación en el método " + method + " con el servidor");
        }
    }

    private void authenticateRemote(Remote remote, byte[] handshakeData, byte[] signature) throws Exception {
        if (remote instanceof Server) {
            if (!security.verifySignature(handshakeData, signature, serverPublicKey)) {
                throw new GeneralSecurityException("No se pudo verificar la autenticidad del servidor durante el saludo");
            }
        } else if (remote instanceof Peer peer) {
            String peerName = peer.getUsername();
            SecretKey authenticationKey = authenticationKeys.get(peerName);
            byte[] expectedChallenge = security.digest(handshakeData, Security.combine(username.getBytes(), peerName.getBytes()));
            if (!Arrays.equals(expectedChallenge, security.decrypt(signature, authenticationKey))) {
                throw new GeneralSecurityException("No se pudo verificar la autenticidad del peer " + peerName + " durante el saludo");
            }
        }
    }

    private byte[] generateResponse(Remote remote, byte[] handshakeData, byte[] signature, SecretKey secretKey) throws Exception {
        if (remote instanceof Server) {
            return security.encrypt(security.digest(handshakeData, signature), secretKey);
        } else if (remote instanceof Peer peer) {
            SecretKey authenticationKey = authenticationKeys.get(peer.getUsername());
            return security.encrypt(security.digest(handshakeData, signature), authenticationKey);
        }
        throw new IllegalArgumentException("No se pudo determinar el tipo del objeto remoto para el que generar la respuesta");
    }

}

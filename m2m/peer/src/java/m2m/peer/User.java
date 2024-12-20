package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.security.Security;
import m2m.shared.security.Security.Ephemeral;
import m2m.shared.Server;

import javax.crypto.SecretKey;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Clase con las funcionalidades principales que exporta la API para los usuarios.
 * Abstrae las llamadas a los procedimientos remotos en llamadas mucho más comprensibles
 * a ser invocadas desde la interfaz gráfica.
 * Gestiona también la seguridad.
 */
public class User {
    public static final String TRUSTED_SERVERS = "/trusted_servers";
    public static final String KEYS_DIRECTORY = "/keys/";

    private final String username;
    private String password;
    private final Security security;
    private final Map<String, Peer> activeFriends;    /* Pares (username, reference) */
    private final Map<String, List<Message>> chats;
    private Peer reference;
    private Server server;
    private Notifier notifier;

    private record AuthenticatedServer(Server server, PublicKey serverKey) {}

    public User(String username, String password) throws Exception {
        Security.ensureNotNull(username, password);
        this.security = new Security();
        this.activeFriends = new HashMap<>();
        Map<String, SecretKey> authenticationKeys = new HashMap<>();
        this.chats = new HashMap<>();
        this.username = username;
        this.password = Security.digest(password, username);

        List<AuthenticatedServer> authenticatedServers = findServers();
        for (AuthenticatedServer authenticatedServer : authenticatedServers) {
            PublicKey serverPublicKey = authenticatedServer.serverKey();
            Server server = authenticatedServer.server();
            Peer reference = new SecurePeer(username, security, activeFriends, authenticationKeys, chats, server, serverPublicKey);
            Ephemeral ephemeral = security.generateEphemeral(server);
            try {
                server.greet(reference, ephemeral.publicKey(), ephemeral.nonce());
                /* Si fue todo bien, guardamos la información relevante */
                this.server = server;
                this.reference = reference;
                this.security.setSelfReference(reference);
                return;
            } catch (GeneralSecurityException exception) {
                /* Si no pudimos autenticar a ese servidor, lo intentamos con el siguiente */
            }
        }
        throw new IllegalStateException("No se pudo localizar ningún servidor conocido activo");
    }

    /* Getters */
    public String getUsername() {
        return username;
    }

    public Peer getReference() {
        return reference;
    }

    public Map<String, Peer> getActiveFriends() {
        return activeFriends;
    }

    public List<Message> getChat(String friend) {
        return chats.get(friend);
    }

    public Notifier getNotifier() {
        return notifier;
    }

    public void setNotifier(Notifier notifier) {
        this.notifier = notifier;
        if (reference instanceof SecurePeer securePeer) {
            securePeer.setNotifier(notifier);
        }
    }

    /* Métodos públicos que exportan la funcionalidad de la API */
    public void greet(String friendName) throws Exception {
        Security.ensureNotNull(friendName);
        Peer friend = activeFriends.get(friendName);
        Ephemeral ephemeral = security.generateEphemeral(friend);
        friend.greet(reference, ephemeral.publicKey(), ephemeral.nonce());
    }

    public void sendMessage(String friendName, String message) throws Exception {
        Security.ensureNotNull(friendName, message);
        Peer friend = activeFriends.get(friendName);
        List<Message> chat = chats.get(friendName);
        Message sent = new Message(message, MessageType.SENT);
        chat.add(sent);
        friend.message(this.reference, security.encrypt(message, friend));
        notifier.notifyMessage(sent, friendName);
    }

    public void signUp() throws Exception {
        String encryptedPassword = security.encrypt(password, server);
        server.signUp(this.reference, encryptedPassword, authenticate(Server.Method.SIGN_UP, encryptedPassword));
    }

    public void login() throws Exception {
        String encryptedPassword = security.encrypt(password, server);
        server.login(this.reference, encryptedPassword, authenticate(Server.Method.LOGIN, encryptedPassword));
    }

    public void logout() throws Exception {
        server.logout(this.reference, authenticate(Server.Method.LOGOUT));
    }

    public void requestFriendship(String friend) throws Exception {
        Security.ensureNotNull(friend);
        server.friendRequest(this.reference, friend, authenticate(Server.Method.FRIEND_REQUEST, friend));
    }

    public void acceptFriendship(String friend) throws Exception {
        Security.ensureNotNull(friend);
        server.friendAccept(this.reference, friend, authenticate(Server.Method.FRIEND_ACCEPT, friend));
        notifier.refreshFriends();
    }

    public void rejectFriendship(String friend) throws Exception {
        Security.ensureNotNull(friend);
        server.friendReject(this.reference, friend, authenticate(Server.Method.FRIEND_REJECT, friend));
    }

    public void removeFriendship(String friend) throws Exception {
        Security.ensureNotNull(friend);
        server.friendRemove(this.reference, friend, authenticate(Server.Method.FRIEND_REMOVE, friend));
    }

    public List<String> searchUsers(String pattern) throws Exception {
        Security.ensureNotNull(pattern);
        return server.searchUsers(this.reference, pattern, authenticate(Server.Method.SEARCH_USERS, pattern));
    }

    public List<String> searchFriends() throws Exception {
        return server.searchFriends(this.reference, authenticate(Server.Method.SEARCH_FRIENDS));
    }

    public List<String> searchPendingRequests() throws Exception {
        return server.searchPendingRequests(this.reference, authenticate(Server.Method.SEARCH_PENDING_REQUESTS));
    }

    public void changePassword(String newPassword) throws Exception {
        Security.ensureNotNull(newPassword);
        String hashedPassword = Security.digest(newPassword, username);
        String encryptedPassword = security.encrypt(hashedPassword, server);
        server.changePassword(this.reference, encryptedPassword, authenticate(Server.Method.CHANGE_PASSWORD, encryptedPassword));
        this.password = hashedPassword; /* Guardarla si se completa correctamente */
    }

    public void deleteUser(String password) throws Exception {
        Security.ensureNotNull(password);
        if(!Security.digest(password, username).equals(this.password)) {
            throw new GeneralSecurityException("Contraseña incorrecta");
        }
        server.deleteUser(this.reference, authenticate(Server.Method.DELETE_USER));
    }

    /* Métodos privados que facilitan la lógica del código */
    private static List<AuthenticatedServer> findServers() throws Exception {
        List<AuthenticatedServer> servers = new ArrayList<>();
        try (InputStream inputStream = User.class.getResourceAsStream(TRUSTED_SERVERS)) {
            if (inputStream == null) {
                throw new IOException("No existe el fichero de servidores confiables");
            }
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] fields = line.split("\\s+");
                    if (fields.length != 3) continue;   /* Ignorar líneas con formato incorrecto */

                    String host = fields[0];

                    int port;
                    try {
                        port = Integer.parseInt(fields[1]);
                    } catch (NumberFormatException exception) {
                        /* Si el puerto no es válido, también ignoramos */
                        continue;
                    }

                    Server server;
                    try {
                        server = serverLookup(host, port);
                    } catch (Exception exception) {
                        /* Si cualquier cosa sale mal, ignoramos */
                        continue;
                    }

                    PublicKey serverPublicKey = Security.loadPublicKey(KEYS_DIRECTORY + fields[2]);

                    servers.add(new AuthenticatedServer(server, serverPublicKey));
                }
            }
        }
        return servers;
    }

    private static Server serverLookup(String host, int port) throws Exception {
//        SecurityInjection.set(security);
        Registry registry = LocateRegistry.getRegistry(host, port);
        return  (Server) registry.lookup(Server.RMI_NAME);
//        SecurityInjection.remove();
    }

    private byte[] authenticate(Server.Method method, Object... parameters) throws Exception {
        Security.ensureNotNull(method, parameters);
        byte[] nonce = Security.generateNonce();
        byte[] serializedData = Security.serialize(method, parameters);
        byte[] hashedAuthentication = Security.digest(serializedData, nonce);
        byte[] authenticationCode = security.encrypt(hashedAuthentication, server);
        return Security.combine(nonce, authenticationCode);
    }
}

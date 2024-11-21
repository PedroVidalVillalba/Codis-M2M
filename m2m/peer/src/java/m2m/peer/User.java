package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Server;

import java.util.Map;

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
    private Map<String, Peer> activeFriends;

    public User(String username, String password) {}

    public boolean sendMessage(Peer peer, String message) {}

    public boolean signUp() {}
    public boolean login() {}
    public boolean logout() {}

    public boolean requestFriend(String friend) {}
    public boolean acceptFriend(String friend) {}
    public boolean rejectFriend(String friend) {}

}

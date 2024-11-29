package m2m.shared;

import java.rmi.Remote;
import java.security.PublicKey;
import java.util.List;

public interface Server extends Remote {
    /* Enumeración con los métodos proporcionados por el servidor, con fines de autenticación */
    enum Method {
        SIGN_UP, LOGIN, LOGOUT, FRIEND_REQUEST, FRIEND_ACCEPT, FRIEND_REJECT, FRIEND_REMOVE, SEARCH_USERS, SEARCH_FRIENDS, SEARCH_PENDING_REQUESTS;

        private boolean requiresLogin;
        static {
            SIGN_UP.requiresLogin = false;
            LOGIN.requiresLogin = false;
            LOGOUT.requiresLogin = true;
            FRIEND_REQUEST.requiresLogin = true;
            FRIEND_ACCEPT.requiresLogin = true;
            FRIEND_REJECT.requiresLogin = true;
            FRIEND_REMOVE.requiresLogin = true;
            SEARCH_USERS.requiresLogin = false;
            SEARCH_FRIENDS.requiresLogin = true;
            SEARCH_PENDING_REQUESTS.requiresLogin = true;
        }
        
        public boolean requiresLogin() {
            return this.requiresLogin;
        }
    }

    /* Nombre con el que los servidores se deben registrar en el registro RMI */
    String RMI_NAME = "m2m.Server";

    /**
     * Recibe un saludo de {@code client} para iniciar una comunicación segura con un usuario.
     * @param client Peer que inicia la comunicación.
     * @param clientPublicKey Clave pública del usuario que inicia la comunicación.
     * @param clientNonce Número aleatorio de un solo uso generado por el cliente.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void greet(Peer client, PublicKey clientPublicKey, byte[] clientNonce) throws Exception;

    /**
     * Intenta registrar un nuevo usuario en el sistema. Método a ser llamado por un peer.
     * @param peer Peer que quiere ser registrado, con un nombre de usuario único.
     * @param password Contraseña cifrada.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void signUp(Peer peer, String password, byte[] authentication) throws Exception;

    /**
     * Intenta iniciar sesión con un usuario ya registrado en el sistema. Método a ser llamado por un peer.
     * @param peer Peer que quiere iniciar sesión, con un nombre de usuario único.
     * @param password Contraseña cifrada.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void login(Peer peer, String password, byte[] authentication) throws Exception;

    /**
     * Intenta cerrar la sesión de un usuario conectado. Método a ser llamado por un peer.
     * @param peer Peer que quiere cerrar sesión.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void logout(Peer peer, byte[] authentication) throws Exception;

    /**
     * Envía una solicitud de amistad al usuario con nombre {@code username}, el cual no tiene por qué estar conectado en ese momento. Método a ser llamado por un peer.
     * @param user Peer que envía la solicitud.
     * @param friendName Nombre de usuario de quien recibe la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendRequest(Peer user, String friendName, byte[] authentication) throws Exception;

    /**
     * Acepta la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param user Peer que está aceptando la solicitud de amistad.
     * @param friendName Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendAccept(Peer user, String friendName, byte[] authentication) throws Exception;

    /**
     * Rechaza la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param user Peer que está rechazando la solicitud de amistad.
     * @param friendName Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendReject(Peer user, String friendName, byte[] authentication) throws Exception;

    /**
     * Elimina al amigo del usuario cuyo nombre coincida con {@code username}. Método a ser llamado por un peer.
     * @param user Peer que está eliminando el amigo.
     * @param friendName Nombre de usuario del amigo a eliminar.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendRemove(Peer user, String friendName, byte[] authentication) throws Exception;

    /**
     * Busca a los usuarios que contengan {@code pattern} en su nombre, con el objetivo de mandarles solicitudes de amistad a continuación. Método a ser llamado por un peer.
     * @param peer Peer que solicita la búsqueda.
     * @param pattern Parte del nombre del usuario buscado. Debe tener al menos 3 caracteres por razones de seguridad.
     * @param authentication Código de autenticación del usuario.
     * @return La lista de usuarios encontrados que coinciden con el patrón.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    List<String> searchUsers(Peer peer, String pattern, byte[] authentication) throws Exception;

    /**
     * Busca a todos los usuarios que sean amigos de quien llama al método. Método a ser llamado por un peer.
     * @param peer Peer que solicita la búsqueda.
     * @param authentication Código de autenticación del usuario.
     * @return La lista de amigos del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    List<String> searchFriends(Peer peer, byte[] authentication) throws Exception;

    /**
     * Busca todas las solicitudes de amistad pendientes de quien llama al método. Método a ser llamado por un peer.
     * @param peer Peer que solicita la búsqueda.
     * @param authentication Código de autenticación del usuario.
     * @return La lista de solicitudes pendientes del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    List<String> searchPendingRequests(Peer peer, byte[] authentication) throws Exception;

    }

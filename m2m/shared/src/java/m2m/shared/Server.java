package m2m.shared;

import java.rmi.Remote;
import java.security.PublicKey;

public interface Server extends Remote {
    /* Cadena de 128 Xs, destinada a ser encriptada y desencriptada para autenticar a los usuarios */
    String AUTHENTICATION_STRING = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

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
    void signUp(Peer peer, String password, String authentication) throws Exception;

    /**
     * Intenta iniciar sesión con un usuario ya registrado en el sistema. Método a ser llamado por un peer.
     * @param peer Peer que quiere iniciar sesión, con un nombre de usuario único.
     * @param password Contraseña cifrada.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void login(Peer peer, String password, String authentication) throws Exception;

    /**
     * Intenta cerrar la sesión de un usuario conectado. Método a ser llamado por un peer.
     * @param peer Peer que quiere cerrar sesión.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void logout(Peer peer, String authentication) throws Exception;

    /**
     * Envía una solicitud de amistad al usuario con nombre {@code username}, el cual no tiene por qué estar conectado en ese momento. Método a ser llamado por un peer.
     * @param peer Peer que envía la solicitud.
     * @param username Nombre de usuario de quien recibe la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendRequest(Peer peer, String username, String authentication) throws Exception;

    /**
     * Acepta la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param peer Peer que está aceptando la solicitud de amistad.
     * @param username Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendAccept(Peer peer, String username, String authentication) throws Exception;

    /**
     * Rechaza la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param peer Peer que está rechazando la solicitud de amistad.
     * @param username Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendReject(Peer peer, String username, String authentication) throws Exception;
}

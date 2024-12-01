package m2m.shared;

import javax.crypto.SecretKey;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Map;

public interface Peer extends Remote {
    /* Enumeración con los métodos proporcionados por el usuario al servidor, con fines de autenticación */
    enum Method {
        ADD_ACTIVE_FRIEND, REMOVE_ACTIVE_FRIEND, FRIEND_REQUEST_RECEIVED
    }

    /**
     * Devuelve el nombre de usuario del peer asociado a esta referencia remota.
     * @return Nombre de usuario.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    String getUsername() throws RemoteException;

    /**
     * Recibe un saludo de {@code greeter} para iniciar una conversación segura.
     * @param greeter Peer que inicia la comunicación.
     * @param greeterPublicKey Clave pública del peer que inicia la comunicación.
     * @param greeterNonce Número aleatorio de un solo uso de quien inicia el saludo.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void greet(Peer greeter, PublicKey greeterPublicKey, byte[] greeterNonce) throws Exception;

    /**
     * Recibe una devolución de saludo de {@code greeted} para iniciar una conversación segura.
     * Este método está pensado para ser llamado desde el método {@link Peer#greet} de otro Peer o desde {@link Server#greet}.
     * @param greeted Remote que responde al saludo.
     * @param greetedPublicKey Clave pública del remote que responde al inicio de comunicación.
     * @param greetedNonce Número aleatorio de un solo uso utilizado por quien recibe el saludo.
     * @param challenge Desafío de autenticación.
     * @return Devuelve un mensaje cifrado para probar que el saludo fue exitoso.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    byte[] greetBack(Remote greeted, PublicKey greetedPublicKey, byte[] greetedNonce, byte[] challenge) throws Exception;

    /**
     * Recibe un mensaje {@code message} de {@code sender}.
     * @param sender Peer que envía el mensaje.
     * @param message Mensaje a enviar.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void message(Peer sender, String message) throws Exception;

    /**
     * Añade un amigo a los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a añadir a los amigos activos.
     * @param encryptedAuthenticationKey Clave encriptada generada por el servidor para la autenticación entre peers.
     * @param authentication Código de autenticación del servidor.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void addActiveFriend(Peer friend, SecretKey encryptedAuthenticationKey, byte[] authentication) throws Exception;

    /**
     * Añade un conjunto de amigos a los amigos en línea. Método a ser llamado por un servidor.
     * @param friends Mapa con pares (username, peer) a añadir a los amigos activos.
     * @param encryptedAuthenticationKeys Mapa con pares (username, key) con las claves para la autenticación entre peers.
     * @param authentication Código de autenticación del servidor.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void addActiveFriend(Map<String, Peer> friends, Map<String, SecretKey> encryptedAuthenticationKeys, byte[] authentication) throws Exception;

    /**
     * Elimina un amigo de los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a eliminar de los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void removeActiveFriend(Peer friend, byte[] authentication) throws Exception;

    /**
     * Indica al usuario que ha recibido una nueva solicitud de amistad. Método a ser llamado por un servidor.
     * @param person Nombre de la persona que le envió la solicitud.
     * @param authentication Código de autenticación del servidor.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    void friendRequestReceived(String person, byte[] authentication) throws Exception;
}

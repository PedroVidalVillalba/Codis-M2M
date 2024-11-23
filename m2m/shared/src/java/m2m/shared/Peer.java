package m2m.shared;

import java.rmi.Remote;
import java.security.PublicKey;
import java.util.Map;

public interface Peer extends Remote {
    /**
     * Devuelve el nombre de usuario del peer asociado a esta referencia remota.
     * @return Nombre de usuario.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    String getUsername() throws Exception;

    /**
     * Recibe un saludo de {@code greeter} para iniciar una conversación segura.
     * @param greeter Peer que inicia la comunicación.
     * @param greeterPublicKey Clave pública del peer que inicia la comunicación.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean greet(Peer greeter, PublicKey greeterPublicKey) throws Exception;

    /**
     * Recibe una devolución de saludo de {@code greeted} para iniciar una conversación segura.
     * Este método está pensado para ser llamado desde el método {@link Peer#greet} de otro Peer o desde {@link Server#greet}.
     * @param greeted Remote que responde al saludo.
     * @param greetedPublicKey Clave pública del peer que responde al inicio de comunicación.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean greetBack(Remote greeted, PublicKey greetedPublicKey) throws Exception;

    /**
     * Recibe un mensaje {@code message} de {@code sender}.
     * @param sender Peer que envía el mensaje.
     * @param message Mensaje a enviar.
     * @return Devuelve si el mensaje fue exitoso o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean message(Peer sender, String message) throws Exception;

    /**
     * Añade un amigo a los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a añadir a los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si el amigo se añadió correctamente o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean addActiveFriend(Peer friend, String authentication) throws Exception;

    /**
     * Añade un conjunto de amigos a los amigos en línea. Método a ser llamado por un servidor.
     * @param friends Mapa con pares (username, peer) a añadir a los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si los amigos se añadieron correctamente o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean addActiveFriend(Map<String, Peer> friends, String authentication) throws Exception;

    /**
     * Elimina un amigo de los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a eliminar de los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si el amigo se eliminó correctamente o no.
     * @throws Exception cuando ocurre algún error en la comunicación remota o con la seguridad.
     */
    boolean removeActiveFriend(Peer friend, String authentication) throws Exception;
}

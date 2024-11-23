package m2m.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

public interface Peer extends Remote {
    /**
     * Devuelve el nombre de usuario del peer asociado a esta referencia remota.
     * @return Nombre de usuario.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    String getUsername() throws RemoteException;

    /**
     * Recibe un saludo de {@code greeter} para iniciar una conversación segura.
     * @param greeter Peer que inicia la comunicación.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean greet(Peer greeter) throws RemoteException;

    /**
     * Recibe una devolución de saludo de {@code greeter} para iniciar una conversación segura.
     * Este método está pensado para ser llamado desde el método {@link Peer#greet} de otro Peer o de un {@link Server}.
     * @param greeter Host que responde al saludo.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean greetBack(Peer greeter) throws RemoteException;

    /**
     * Recibe un mensaje {@code message} de {@code sender}.
     * @param sender Peer que envía el mensaje.
     * @param message Mensaje a enviar.
     * @return Devuelve si el mensaje fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean message(Peer sender, String message) throws RemoteException;

    /**
     * Añade un amigo a los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a añadir a los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si el amigo se añadió correctamente o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean addActiveFriend(Peer friend, String authentication) throws RemoteException;

    /**
     * Añade un conjunto de amigos a los amigos en línea. Método a ser llamado por un servidor.
     * @param friends Mapa con pares (username, peer) a añadir a los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si los amigos se añadieron correctamente o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean addActiveFriend(Map<String, Peer> friends, String authentication) throws RemoteException;

    /**
     * Elimina un amigo de los amigos en línea. Método a ser llamado por un servidor.
     * @param friend Amigo a eliminar de los amigos activos.
     * @param authentication Código de autenticación del servidor.
     * @return Devuelve si el amigo se eliminó correctamente o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean removeActiveFriend(Peer friend, String authentication) throws RemoteException;
}

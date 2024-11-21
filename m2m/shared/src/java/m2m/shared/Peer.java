package m2m.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Peer extends Remote {
    /**
     * Recibe un saludo de {@code greeter} para iniciar una conversación segura.
     * @param greeter Peer que inicia la comunicación.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean greet(Peer greeter) throws RemoteException;

    /**
     * Recibe un mensaje {@code message} de {@code sender}.
     * @param sender Peer que envía el mensaje.
     * @param message Mensaje a enviar.
     * @return Devuelve si el mensaje fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean message(Peer sender, String message) throws RemoteException;

    /**
     * Devuelve el nombre de usuario del peer asociado a esta referencia remota.
     * @return Nombre de usuario.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    String getUsername() throws RemoteException;
}

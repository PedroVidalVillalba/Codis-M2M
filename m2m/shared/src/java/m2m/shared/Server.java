package m2m.shared;

import java.rmi.Remote;
import java.security.PublicKey;
import java.rmi.RemoteException;

public interface Server extends Remote {
    /* Cadena de 128 X's, destinada a ser encriptada y desencriptada para autenticar a los usuarios */
    String AUTHENTICATION_STRING = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    /**
     * Recibe un saludo de {@code client} para iniciar una comunicación segura con un usuario.
     * @param client Peer que inicia la comunicación.
     * @param clientPublicKey Clave pública del usuario durante el intercambio Diffie-Hellman.
     * @return Devuelve si el saludo fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean greet(Peer client, PublicKey clientPublicKey) throws RemoteException;

    /**
     * Intenta registrar un nuevo usuario en el sistema. Método a ser llamado por un peer.
     * @param peer Peer que quiere ser registrado.
     * @param username Nombre de usuario único.
     * @param password Contraseña cifrada.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si el registro fue existoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean signUp(Peer peer, String username, String password, String authentication) throws RemoteException;
    /**
     * Intenta iniciar sesión con un usuario ya registrado en el sistema. Método a ser llamado por un peer.
     * @param peer Peer que quiere iniciar sesión.
     * @param username Nombre de usuario único.
     * @param password Contraseña cifrada.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si el inicio de sesión fue existoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean login(Peer peer, String username, String password, String authentication) throws RemoteException;

    /**
     * Intenta cerrar la sesión de un usuario conectado. Método a ser llamado por un peer.
     * @param peer Peer que quiere cerrar sesión.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si el cierre de sesión fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean logout(Peer peer, String authentication) throws RemoteException;

    /**
     * Envía una solicitud de amistad al usuario con nombre {@code username}, el cual no tiene por qué estar conectado en ese momento. Método a ser llamado por un peer.
     * @param peer Peer que envía la solicitud.
     * @param username Nombre de usuario de quien recibe la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si el envío de la petición fue exitoso o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean friendRequest(Peer peer, String username, String authentication) throws RemoteException;

    /**
     * Acepta la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param peer Peer que está aceptando la solicitud de amistad.
     * @param username Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si se ha aceptado la petición correctamente o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean friendAccept(Peer peer, String username, String authentication) throws RemoteException;

    /**
     * Rechaza la solicitud de amistad enviada por el usuario con nombre {@code username}. Método a ser llamado por un peer.
     * @param peer Peer que está rechazando la solicitud de amistad.
     * @param username Nombre de usuario de quien envió la solicitud.
     * @param authentication Código de autenticación del usuario.
     * @return Devuelve si se ha rechazado la petición correctamente o no.
     * @throws RemoteException cuando ocurre algún error en la comunicación remota.
     */
    boolean friendReject(Peer peer, String username, String authentication) throws RemoteException;
}

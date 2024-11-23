package m2m.shared;

import java.rmi.Remote;
import java.security.PublicKey;

public interface Server extends Remote {
    /* Cadena de 128 X's, destinada a ser encriptada y desencriptada para autenticar a los usuarios */
    String AUTHENTICATION_STRING = "XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX";

    boolean greet(Peer client, PublicKey clientPublicKey);

    boolean signUp(Peer peer, String username, String password, String authentication);
    boolean login(Peer peer, String username, String password, String authentication);
    boolean logout(Peer peer, String authentication);

    boolean friendRequest(Peer peer, String username, String authentication);
    boolean friendAccept(Peer peer, String username, String authentication);
    boolean friendReject(Peer peer, String username, String authentication);
}

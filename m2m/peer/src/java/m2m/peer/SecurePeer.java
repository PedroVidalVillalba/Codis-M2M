package m2m.peer;

import m2m.shared.Peer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SecurePeer extends UnicastRemoteObject implements Peer {
    final private String username;

    public SecurePeer(String username) throws RemoteException {
        super();
        if (username == null) throw new NullPointerException("El nombre de usuario no puede ser null");
        this.username = username;
    }

    public boolean greet(Peer greeter) throws RemoteException {
        return false;
    }

    @Override
    public boolean message(Peer sender, String message) throws RemoteException {
        return false;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }
}

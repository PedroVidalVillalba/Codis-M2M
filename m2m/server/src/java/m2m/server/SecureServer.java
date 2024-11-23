package m2m.server;

import m2m.shared.Server;
import m2m.shared.Security;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class SecureServer extends UnicastRemoteObject implements Server{

    private Database database;
    private Security security;

    public SecureServer() throws RemoteException{
        super();
        database = DataBase.getCurrentDatabase();
        this.security = new Security();
    }

    /* TODO: copiar método greet de SecurePeer */

    public static void main(String[] args) {
        SecureServer prueba = new SecureServer();
        List<String> usernames = database.getUsernames();
        System.out.println(usernames);
    }
}

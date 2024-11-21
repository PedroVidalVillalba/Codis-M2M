package m2m.server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ServerImpl extends UnicastRemoteObject implements Server{

    private Database database;

    public ServerImpl() throws RemoteException{
        super();
        database = Database.getCurrentDatabase();
    }


    public static void main(String[] args) {
        ServerImpl prueba = new ServerImpl();
        List<String> usernames = database.getUsernames();
        System.out.println(usernames);


    }
}

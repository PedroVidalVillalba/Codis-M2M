package m2m.server;

import m2m.shared.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        Registry registry = startRegistry(1099);
        Server server = new SecureServer();
        registry.rebind(Server.RMI_NAME, server);

        System.out.println("Server started");
        System.out.println("Objetos exportados en el registro RMI:");
        for (String bound : registry.list()) {
            System.out.println("\t" + bound);
        }
        System.out.println("Press enter to exit...");
        System.in.read();
        registry.unbind(Server.RMI_NAME);
        System.out.println("Server stopped");
        DataBase.closeCurrentDB();
        System.exit(0);
    }

    private static Registry startRegistry(int port) throws RemoteException {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(port);
            registry.list();	/* Lanza una excepción si el registro no existe */
        } catch (RemoteException e) {	/* No hay un registro RMI existente en el puerto especificado */
            System.out.println("No se pudo localizar un registro RMI en el puerto " + port);
            registry = LocateRegistry.createRegistry(port);
            System.out.println("Creado un nuevo registro RMI en el puerto " + port);
        }
        return registry;
    }
}

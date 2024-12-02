package m2m.server;

import m2m.shared.Server;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.RemoteException;

public class ServerMain {
    public static final int RMI_PORT = 1099;

    public static void main(String[] args) throws Exception {
        Registry registry = startRegistry();
        Server server = new SecureServer();
        registry.rebind(Server.RMI_NAME, server);

        Runtime.getRuntime().exec(new String[] {"notify-send", Server.RMI_NAME + " iniciado en el puerto " + RMI_PORT});

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                registry.unbind(Server.RMI_NAME);
                DataBase.closeCurrentDB();
                Runtime.getRuntime().exec(new String[] {"notify-send", Server.RMI_NAME + " desconectado."});
            } catch (Exception exception) {
                System.err.println(exception.getMessage());
            }
        }));
    }

    private static Registry startRegistry() throws RemoteException {
        Registry registry;
        try {
            registry = LocateRegistry.getRegistry(RMI_PORT);
            registry.list();	/* Lanza una excepción si el registro no existe */
        } catch (RemoteException e) {	/* No hay un registro RMI existente en el puerto especificado */
            registry = LocateRegistry.createRegistry(RMI_PORT);
        }
        return registry;
    }
}

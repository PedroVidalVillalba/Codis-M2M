package m2m.shared.security;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SecureServerSocket extends ServerSocket {
    private final Security security;

    public SecureServerSocket(int port, Security security) throws IOException {
        super(port);
        this.security = security;
    }

    public Socket accept() throws IOException {
        SecureSocket secureSocket = new SecureSocket(security);
        implAccept(secureSocket);
        return secureSocket;
    }
}

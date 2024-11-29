package m2m.shared.security;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.server.RMIServerSocketFactory;
import java.util.Objects;

public class SecureServerSocketFactory implements RMIServerSocketFactory {
    private final Security security;

    public SecureServerSocketFactory(Security security) {
        this.security = security;
    }

    @Override
    public ServerSocket createServerSocket(int port) throws IOException {
        return new SecureServerSocket(port, security);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SecureServerSocketFactory that)) return false;
        return Objects.equals(security, that.security);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(security);
    }
}
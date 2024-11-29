package m2m.shared.security;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serial;
import java.io.Serializable;
import java.net.Socket;
import java.rmi.Remote;
import java.rmi.server.RMIClientSocketFactory;
import java.util.Objects;

public class SecureClientSocketFactory implements RMIClientSocketFactory, Serializable {
    public static class SecurityInjection {
        private static Security securityInjection = null;

        public static synchronized void set(Security security) {
            securityInjection = security;
        }

        public static synchronized void remove() {
            securityInjection = null;
        }

        public static synchronized Security get() {
            return securityInjection;
        }
    }

    private transient Security security;    /* Marcar como transient para que no se serialice */
    private final Remote receiver;

    public SecureClientSocketFactory(Remote receiver) {
        this.receiver = receiver;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        if (security == null) {
            throw new IllegalStateException("Debe configurarse la instancia de seguridad antes de usar la fábrica");
        }
        return new SecureSocket(host, port, security, receiver);
    }

    @Serial
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        System.out.println("Leído la fábrica de sockets");

        /* Establecer la seguridad, que no se serializa */
        if (this.security == null) {
            this.security = SecurityInjection.get();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SecureClientSocketFactory that)) return false;
        return Objects.equals(receiver, that.receiver);
    }

    @Override
    public int hashCode() {
        return Objects.hash(security, receiver);
    }
}

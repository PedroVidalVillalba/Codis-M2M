package m2m.shared.security;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.rmi.Remote;
import java.security.GeneralSecurityException;

public class SecureSocket extends Socket {
    private InputStream in = null;
    private OutputStream out = null;
    private final Security security;
    private Remote receiver;

    /**
     * Constructor por defecto para SecureSocket.
     * Este solo se debería usar si se realiza inicialización adicional después de crear la instancia.
     */
    public SecureSocket(Security security) {
        super();
        this.security = security;
    }

    public SecureSocket(String host, int port, Security security, Remote receiver) throws IOException {
        super(host, port);
        this.security = security;
        this.receiver = receiver;
    }

    public void setReceiver(Remote receiver) {
        this.receiver = receiver;
    }

    @Override
    public synchronized InputStream getInputStream() throws IOException {
        if (in == null) {
            try {
                /* El socket se pasa a sí mismo como dependencia inversa, para que cuando
                 * el InputStream determine al emisor, se pueda configurar al receptor de este
                 * socket con esa referencia para poder responderle de forma segura */
                in = new SecureInputStream(super.getInputStream(), security, this);
            } catch (GeneralSecurityException exception) {
                /* En caso de que no haya claves disponibles, fallback al InputStream estándar
                 * No cachear el stream para que en la siguiente conexión se vuelva a intentar crear uno seguro */
                return super.getInputStream();
            }
        }
        return in;
    }

    @Override
    public synchronized OutputStream getOutputStream() throws IOException {
        if (out == null) {
            try {
                out = new SecureOutputStream(super.getOutputStream(), security, receiver);
            } catch (GeneralSecurityException | IllegalArgumentException exception) {
                /* En caso de que no haya claves disponibles, fallback al OutputStream estándar
                 * No cachear el stream para que en la siguiente conexión se vuelva a intentar crear uno seguro */
                return super.getOutputStream();
            }
        }
        return out;
    }
}

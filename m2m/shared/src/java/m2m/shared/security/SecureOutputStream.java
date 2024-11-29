package m2m.shared.security;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.rmi.Remote;
import java.security.GeneralSecurityException;

public class SecureOutputStream extends FilterOutputStream {
    public SecureOutputStream(OutputStream out, Security security, Remote receiver) throws GeneralSecurityException, IOException {
        super(security.encryptStream(out, receiver));
    }
}

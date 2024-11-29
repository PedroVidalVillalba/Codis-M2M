package m2m.shared.security;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class SecureInputStream extends FilterInputStream {
    public SecureInputStream(InputStream in, Security security, SecureSocket socket) throws GeneralSecurityException, IOException {
        super(security.decryptStream(in, socket));
    }
}

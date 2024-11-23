package m2m.peer;

import java.rmi.Remote;

public class PeerSecurity {
    public String encrypt(String string, Remote remote) {
        return string;
    }

    public String decrypt(String string, Remote remote) {
        return string;
    }

    public String digest(String string, String salt) {
        return string;
    }

}

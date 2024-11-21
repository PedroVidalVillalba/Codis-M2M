package m2m.shared;

public interface Server {
    boolean greet(Peer peer);

    boolean signUp(Peer peer, String username, String password);
    boolean login(Peer peer, String username, String password);
    boolean logout(Peer peer);

    boolean friendRequest(Peer peer, String username);
    boolean friendAccept(Peer peer, String username);
    boolean friendReject(Peer peer, String username);
}

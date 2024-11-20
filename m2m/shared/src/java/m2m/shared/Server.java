package m2m.shared;

public interface Server {
    void greet(Peer peer);

    boolean login(Peer peer, String username, String password);
    boolean signUp(Peer peer, String username, String password);
    boolean logout(Peer peer);

    boolean friendRequest(Peer peer, String username);
    boolean friendAccept(Peer peer, String username);
    boolean friendReject(Peer peer, String username);
}

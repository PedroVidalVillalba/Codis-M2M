package m2m.shared;

public interface Peer {
    void greet(Peer peer);

    void message(String message);

    String getUsername();
}

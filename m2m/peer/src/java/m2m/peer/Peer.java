package m2m.peer;

public interface Peer {
    void greet(Peer peer);

    void message(String message);

    String getUsername();
}

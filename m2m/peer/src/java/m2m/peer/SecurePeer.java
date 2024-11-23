package m2m.peer;

import m2m.shared.Peer;
import m2m.shared.Server;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SecurePeer extends UnicastRemoteObject implements Peer {
    final private String username;
    private PeerSecurity security;
    private Map<String, Peer> activeFriends;
    private Map<String, List<Message>> chats;
    private Server server;

    public SecurePeer(String username, PeerSecurity security, Map<String, Peer> activeFriends, Map<String, List<Message>> chats, Server server) throws RemoteException {
        super();
        this.username = username;
        this.security = security;
        this.activeFriends = activeFriends;
        this.chats = chats;
        this.server = server;
    }

    @Override
    public String getUsername() throws RemoteException {
        return this.username;
    }

    @Override
    public boolean greet(Peer greeter) throws RemoteException {
        return false;
    }

    @Override
    public boolean greetBack(Peer greeter) throws RemoteException {
        return false;
    }

    @Override
    public boolean message(Peer sender, String message) throws RemoteException {
        Peer friend = activeFriends.get(sender.getUsername());
        if (friend == null) return false;
        security.decrypt(message, friend);
        List<Message> chat = chats.get(friend.getUsername());
        chat.add(new Message(message, MessageType.RECEIVED));
        return true;
    }

    @Override
    public boolean addActiveFriend(Peer friend, String authentication) throws RemoteException {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.put(friend.getUsername(), friend);
        chats.put(friend.getUsername(), new ArrayList<>());
        return true;
    }

    @Override
    public boolean addActiveFriend(Map<String, Peer> friends, String authentication) throws RemoteException {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.putAll(friends);
        for (String friend : activeFriends.keySet()) {
            chats.put(friend, new ArrayList<>());
        }
        return true;
    }

    @Override
    public boolean removeActiveFriend(Peer friend, String authentication) throws RemoteException {
        if (serverFailedToAuthenticate(authentication)) return false;
        activeFriends.remove(friend.getUsername());
        chats.remove(friend.getUsername());
        return true;
    }

    private boolean serverFailedToAuthenticate(String authentication) throws RemoteException {
        return !security.decrypt(authentication, server).equals(Server.AUTHENTICATION_STRING);
    }
}

package m2m.peer.gui;

import m2m.peer.Message;
import m2m.peer.Notifier;

import java.util.function.Consumer;

public class NotifierGUI implements Notifier {
    private Consumer<String> addActiveFriend = friendName -> {} ;
    private Consumer<String> removeActiveFriend = friendName -> {} ;
    private Consumer<Message> message = message -> {} ;
    private Consumer<String> allFriends = allFriends -> {} ;
    private Consumer<String> refreshFriends = friendName -> {} ;
    private Consumer<String> refreshFriendRequests = personName -> {} ;


    public NotifierGUI() {

    }

    public void setNotifyAddActiveFriend(Consumer<String> addActiveFriend){
        this.addActiveFriend = addActiveFriend;
    }

    public void setNotifyRemoveActiveFriend(Consumer<String> removeActiveFriend){
        this.removeActiveFriend = removeActiveFriend;
    }

    public void setNotifyMessage(Consumer<Message> message){
        this.message = message;
    }

    public void setNotifyAllFriendsConnected(Consumer<String> allFriends) {
        this.allFriends = allFriends;
    }

    public void setRefreshFriends(Consumer<String> friendName) {
        this.refreshFriends = friendName;
    }

    public void setRefreshFriendrequests(Consumer<String> personName) {
        this.refreshFriendRequests = personName;
    }



    public void notifyAddActiveFriend(String friendName) {
        addActiveFriend.accept(friendName);
    }

    public void notifyRemoveActiveFriend(String friendName) {
        removeActiveFriend.accept(friendName);
    }

    public void notifyMessage(Message message) {
        this.message.accept(message);
    }

    public void notifyAllFriendsConnected(String allFriends) {
        this.allFriends.accept(allFriends);
    }

    public void refreshFriends(String friendName) {
        refreshFriends.accept(friendName);
    }

    public void refreshFriendRequests(String personName) {
        refreshFriendRequests.accept(personName);
    }
}

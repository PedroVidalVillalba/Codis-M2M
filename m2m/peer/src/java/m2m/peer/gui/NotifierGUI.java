package m2m.peer.gui;

import m2m.peer.Message;
import m2m.peer.Notifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NotifierGUI implements Notifier {
    private Consumer<String> notifyAddActiveFriend = friendName -> {} ;
    private Consumer<String> notifyRemoveActiveFriend = friendName -> {} ;
    private BiConsumer<Message, String> notifyMessage = (message, friendName) -> {} ;
    private Consumer<String> allFriends = allFriends -> {} ;
    private Consumer<String> personName = personName -> {} ;
    private Consumer<String> refreshFriends = friendName -> {} ;
    private Consumer<String> refreshFriendRequests = personName -> {} ;


    @Override
    public void setNotifyAddActiveFriend(Consumer<String> notifyAddActiveFriend){
        this.notifyAddActiveFriend = notifyAddActiveFriend;
    }

    @Override
    public void setNotifyRemoveActiveFriend(Consumer<String> notifyRemoveActiveFriend){
        this.notifyRemoveActiveFriend = notifyRemoveActiveFriend;
    }

    @Override
    public void setNotifyMessage(BiConsumer<Message, String> notifyMessage){
        this.notifyMessage = notifyMessage;
    }

    public void setNotifyAllFriendsConnected(Consumer<String> allFriends) {
        this.allFriends = allFriends;
    }

    public void setNotifyNewFriendRequest(Consumer<String> personName) {
        this.personName = personName;
    }

    public void setRefreshFriends(Consumer<String> friendName) {
        this.refreshFriends = friendName;
    }

    public void setRefreshFriendRequests(Consumer<String> personName) {
        this.refreshFriendRequests = personName;
    }


    @Override
    public void notifyAddActiveFriend(String friendName) {
        notifyAddActiveFriend.accept(friendName);
    }

    @Override
    public void notifyRemoveActiveFriend(String friendName) {
        notifyRemoveActiveFriend.accept(friendName);
    }

    @Override
    public void notifyMessage(Message message, String friendName) {
        notifyMessage.accept(message, friendName);
    }

    @Override
    public void notifyAllFriendsConnected(String allFriends) {
        this.allFriends.accept(allFriends);
    }

    @Override
    public void notifyNewFriendRequest(String personName) {
        this.personName.accept(personName);
    }

    @Override
    public void refreshFriends(String friendName) {
        refreshFriends.accept(friendName);
    }

    @Override
    public void refreshFriendRequests(String personName) {
        refreshFriendRequests.accept(personName);
    }
}

package m2m.peer;

import java.util.function.Consumer;

public interface Notifier {

    void setNotifyAddActiveFriend(Consumer<String> addActiveFriend);

    void setNotifyRemoveActiveFriend(Consumer<String> removeActiveFriend);

    void setNotifyMessage(Consumer<Message> message);

    void setNotifyAllFriendsConnected(Consumer<String> allFriends);

    void setRefreshFriends(Consumer<String> friendName);

    void setRefreshFriendrequests(Consumer<String> personName);


    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(Message message);

    void notifyAllFriendsConnected(String allFriends);

    void refreshFriends(String friendName);

    void refreshFriendRequests(String personName);
}

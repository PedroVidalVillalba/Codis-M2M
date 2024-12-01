package m2m.peer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Notifier {

    void setNotifyAddActiveFriend(Consumer<String> notifyAddActiveFriend);

    void setNotifyRemoveActiveFriend(Consumer<String> notifyRemoveActiveFriend);

    void setNotifyMessage(BiConsumer<Message, String> notifyMessage);

    void setNotifyAllFriendsConnected(Consumer<String> allFriends);

    void setNotifyNewFriendRequest(Consumer<String> personName);

    void setRefreshFriends(Consumer<String> friendName);

    void setRefreshFriendRequests(Consumer<String> personName);


    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(Message message, String friendName);

    void notifyAllFriendsConnected(String allFriends);

    void notifyNewFriendRequest(String personName);

    void refreshFriends(String friendName);

    void refreshFriendRequests(String personName);
}

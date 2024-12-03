package m2m.peer;

public interface Notifier {
    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(Message message, String friendName);

    void notifyAllFriendsConnected(String allFriends);

    void notifyNewFriendRequest(String personName);

    void refreshFriends();
}

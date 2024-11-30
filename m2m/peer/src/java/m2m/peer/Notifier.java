package m2m.peer;

import java.util.function.Consumer;

public interface Notifier {

    void setNotifyAddActiveFriend(Consumer<String> addActiveFriend);

    void setNotifyRemoveActiveFriend(Consumer<String> removeActiveFriend);

    void setNotifyMessage(Consumer<Message> message);

    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(Message message);
}

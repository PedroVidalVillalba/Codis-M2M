package m2m.peer;

import java.util.function.Consumer;

public interface Notifier {

    void setNotifyAddActiveFriend(Consumer<String> addActiveFriend);

    void setNotifyRemoveActiveFriend(Consumer<String> removeActiveFriend);

    void setNotifyMessage(Consumer<String> message);

    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(String message);



}

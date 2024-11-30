package m2m.peer;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface Notifier {

    void setNotifyAddActiveFriend(Consumer<String> notifyAddActiveFriend);

    void setNotifyRemoveActiveFriend(Consumer<String> notifyRemoveActiveFriend);

    void setNotifyMessage(BiConsumer<Message, String> notifyMessage);

    void notifyAddActiveFriend(String friendName);

    void notifyRemoveActiveFriend(String friendName);

    void notifyMessage(Message message, String friendName);
}

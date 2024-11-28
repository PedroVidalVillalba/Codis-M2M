package m2m.peer;

import java.util.function.Consumer;

public interface Notifier {

    void setAddActiveFriend(Consumer<String> addActiveFriend);

    void setRemoveActiveFriend(Consumer<String> removeActiveFriend);

    void setMessage(Consumer<String> message);

    void addActiveFriend(String friendName);

    void removeActiveFriend(String friendName);

    void message(String message);



}

package m2m.peer.gui;

import m2m.peer.Message;
import m2m.peer.Notifier;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class NotifierGUI implements Notifier {
    private Consumer<String> notifyAddActiveFriend = friendName -> {} ;
    private Consumer<String> notifyRemoveActiveFriend = friendName -> {} ;
    private BiConsumer<Message, String> notifyMessage = (message, friendName) -> {} ;

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
        this.notifyMessage.accept(message, friendName);
    }
}

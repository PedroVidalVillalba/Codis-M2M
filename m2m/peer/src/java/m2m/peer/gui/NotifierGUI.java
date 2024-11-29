package m2m.peer.gui;

import m2m.peer.Notifier;

import java.util.function.Consumer;

public class NotifierGUI implements Notifier {
    private Consumer<String> addActiveFriend = friendName -> {} ;
    private Consumer<String> removeActiveFriend = friendName -> {} ;
    private Consumer<String> message = message -> {} ;


    public NotifierGUI() {

    }

    public void setNotifyAddActiveFriend(Consumer<String> addActiveFriend){
        this.addActiveFriend = addActiveFriend;
    }

    public void setNotifyRemoveActiveFriend(Consumer<String> removeActiveFriend){
        this.removeActiveFriend = removeActiveFriend;
    }

    public void setNotifyMessage(Consumer<String> message){
        this.message = message;
    }



    public void notifyAddActiveFriend(String friendName) {
        addActiveFriend.accept(friendName);
    }

    public void notifyRemoveActiveFriend(String friendName) {
        removeActiveFriend.accept(friendName);
    }

    public void notifyMessage(String message) {
        this.message.accept(message);
    }
}

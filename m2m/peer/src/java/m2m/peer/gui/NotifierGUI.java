package m2m.peer.gui;

import m2m.peer.Notifier;

import java.util.function.Consumer;

public class NotifierGUI implements Notifier {
    private Consumer<String> addActiveFriend = friendName -> {} ;
    private Consumer<String> removeActiveFriend = friendName -> {} ;
    private Consumer<String> message = message -> {} ;


    public NotifierGUI() {

    }

    public void setAddActiveFriend(Consumer<String> addActiveFriend){
        this.addActiveFriend = addActiveFriend;
    }

    public void setRemoveActiveFriend(Consumer<String> removeActiveFriend){
        this.removeActiveFriend = removeActiveFriend;
    }

    public void setMessage(Consumer<String> message){
        this.message = message;
    }



    public void addActiveFriend(String friendName) {
        addActiveFriend.accept(friendName);
    }

    public void removeActiveFriend(String friendName) {
        removeActiveFriend.accept(friendName);
    }

    public void message(String message) {
        this.message.accept(message);
    }
}

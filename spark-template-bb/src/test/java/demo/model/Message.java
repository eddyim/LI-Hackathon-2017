package demo.model;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private String _message;

    public String getMessage() {
        return _message;
    }

    public void setMessage(String message) {
        _message = message;
    }

    private Message(String msg) {
        _message = msg;
    }

    private static final List<Message> MESSAGES = new ArrayList<>();

    public static List<Message> getAllMessages() {
        return MESSAGES;
    }

    public static void addMessage(String msg) {
        MESSAGES.add(new Message(msg));
    }

}

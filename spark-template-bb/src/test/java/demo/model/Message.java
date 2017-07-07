package demo.model;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private static String _user;
    private String _message;

    public String getMessage() {
        return _message;
    }

    public static String getUser() {
        return _user;
    }

    public static void setUser(String user) {
        _user = user;
    }

    public void setMessage(String _message) {
        _message = _message;
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

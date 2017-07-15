package demo.model;

import java.util.ArrayList;
import java.util.List;

public class Message {

    private static String _user;
    private String _message;
    private String _sender;

    public String getMessage() {
        return _message;
    }

    public static String getUser() {
        return _user;
    }

    public static void setUser(String user) {
        _user = user;
    }

    public String getSender() { return _sender;}

    public void setSender(String _sender) {this._sender = _sender;}

    public void setMessage(String _message) {
        this._message = _message;
    }

    private Message(String snd, String msg) {
        _sender = snd;
        _message = msg;
    }

    private static final List<Message> MESSAGES = new ArrayList<>();

    public static List<Message> getAllMessages() {
        return MESSAGES;
    }

    public static void addMessage(String msg) {
        MESSAGES.add(new Message("", msg));
    }

    public static void addMessage(String snd, String msg) { MESSAGES.add(new Message(snd, msg));}
}
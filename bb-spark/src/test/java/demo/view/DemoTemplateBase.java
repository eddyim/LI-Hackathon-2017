package demo.view;

import bb.sparkjava.BBSparkTemplate;
import demo.model.Message;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class DemoTemplateBase extends BBSparkTemplate {
    public static Set<String> getUsers() {
        List<Message> allMessages = Message.getAllMessages();
        Set<String> collect = allMessages.stream().map(Message::getSender).collect(Collectors.toSet());
        return new TreeSet<>(collect);
    }
}

package demo;

import bb.sparkjava.BBSparkTemplate;
import com.notification.NotificationFactory;
import com.notification.NotificationManager;
import com.notification.manager.QueueManager;
import com.notification.manager.SimpleManager;
import com.notification.types.TextNotification;
import com.theme.ThemePackagePresets;
import com.utils.Time;
import demo.model.*;
import demo.views.*;

import javax.management.Notification;
import java.util.List;


import static spark.Spark.*;

public class ApplicationServer {


    public static void main(String[] args) {

        staticFileLocation("/static");

        UserBase.addDummyData();
        BBSparkTemplate.init();

        get("/", (req, resp) -> {
            if (req.session().attribute("userName") == null) {
                resp.redirect("/login");

                return null;
            } else {
                return Index.render(Message.getAllMessages(), req.session().attribute("userName"));
            }
        });

        get("/login", (req, resp) -> {
            if (req.session().attribute("account")!= null) {
                resp.redirect("/dashboard");
                return null;
            } else {
                if (req.session().attribute("loginErrorMessage") == null) {
                    return Login.render("");
                } else {
                    return Login.render(req.session().attribute("loginErrorMessage"));
                }
            }
        });

        post("/login", (req, resp) -> {
            String username = req.queryParams("username");
            String password = req.queryParams("password");
            try {
                UserBase.Account currentAccount = UserBase.validateLogin(username, password);
                req.session().attribute("account", currentAccount);
                req.session().attribute("loginErrorMessage", null);
                resp.redirect("/dashboard");
            } catch (UserRuntimeException e) {
                String errorMessage = e.toString();
                req.session().attribute("loginErrorMessage", errorMessage.substring(errorMessage.indexOf("ERROR")));
                resp.redirect("/login");
            }
            return null;
        });

        get("/dashboard", (req, resp) -> {
            if (req.session().attribute("account") == null) {
                resp.redirect("/");
                return null;
            }
            return Dashboard.render(req.session().attribute("account"));
        });

        get("/messages", (req, resp) -> Index.messageBox.render(Message.getAllMessages()));

        post("/messages", (req, resp) -> {
            String message = req.queryParams("message");
            if (message != null && message.length() > 0) {
                Message.addMessage(req.session().attribute("userName"), message);
            }
            return Index.inputForm.render();
        });

        get("/who", (req, resp) -> Index.who.render());
    }
}

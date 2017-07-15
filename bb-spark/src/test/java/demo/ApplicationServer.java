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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import static spark.Spark.*;

public class ApplicationServer {


    public static void main(String[] args) {

        staticFileLocation("/static");

        UserBase.addDummyData();
        BBSparkTemplate.init();

        get("/", (req, resp) -> First.render());

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

        get("/register", (req, resp) -> Signup.render());

        post("/register", (req, resp) -> {
           String username = req.queryParams("username");
           String password = req.queryParams("pwd");
           String first = req.queryParams("fname");
           String last = req.queryParams("lname");
           String team = req.queryParams("team");
           String role = req.queryParams("role");
           List<String> restrictions = new ArrayList<>();
           List<String> cuisine = new ArrayList<>();
           String[] rest = req.queryParams("restrictions").split(",");
           String[] cuis = req.queryParams("cuisine").split(",");
           for(String r: rest) {
               restrictions.add(r.trim());
           }
           for(String c: cuis) {
               cuisine.add(c.trim());
           }
           UserBase.addAccount(username, password, first, last, team, role, restrictions,
                   cuisine, new ArrayList<>(), new ArrayList<>(), "");
           resp.redirect("/login");
           return null;
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

        get("/confirm/:confirmWith", (req, resp) -> {
            UserBase.Account currentAcct = req.session().attribute("account");
            String firstName = req.params(":confirmWith");
            if (currentAcct == null) {
                resp.redirect("/login");
                return null;
            }
            List<UserPairing> pairings = UserBase.getTopPairings(currentAcct.getUser());
            for (UserPairing pair: pairings) {
                if (pair.getSecond().getFirstName().equals(firstName)) {
                    return ConfirmPage.render(pair);
                }
            }
            return null;
        });

        get("/confirmationSuccess", (req, resp) -> {
            NotificationFactory factory = new NotificationFactory(ThemePackagePresets.cleanLight());
            NotificationManager plain = new SimpleManager(NotificationFactory.Location.NORTHEAST);
            TextNotification notification = factory.buildTextNotification("Lunchmeet Success!",
                    "A notification has been sent.");
            notification.setCloseOnClick(true);
            plain.addNotification(notification, Time.seconds(5));
            resp.redirect("/dashboard");
            return null;
        });

        get("/logout", (req, resp) -> {
            req.session().attribute("account", null);
            resp.redirect("/login");
            return null;
        });
    }
}

package demo;

import bb.sparkjava.BBSparkTemplate;

import static spark.Spark.*;

public class ApplicationServer {


    public static void main(String[] args) {

        staticFileLocation("/static");

        BBSparkTemplate.init();

        get("/", (req, resp) -> {
            if (req.session().attribute("userName") == null) {
                resp.redirect("/login");
                return null;
            } else {
                return Index.render(Message.getAllMessages(), req.session().attribute("userName"));
            }
        });

        get("/login", (req, resp) -> Login.render());

        post("/login", (req, resp) -> {
            String login = req.queryParams("userName");
            if (login.length() > 0) {
                req.session().attribute("userName", login);
                Message.setUser(req.session().attribute("userName"));
                resp.redirect("/");
            } else {
                resp.redirect("/login");
            }
            return null;
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

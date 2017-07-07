package demo;

import demo.views.*;
import bb.sparkjava.BBSparkTemplate;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

public class DemoServer {


    public static void main(String[] args) {

        staticFileLocation("/static");

        BBSparkTemplate.init();

        get("/", (req, resp) -> Index.render() ); // <-- make this work

    }
}

package demo;

import bb.sparkjava.BBSparkTemplate;
import demo.views.*;

import static spark.Spark.*;

public class DemoServer {


    public static void main(String[] args) {

        staticFileLocation("/static");

        BBSparkTemplate.init();

        get("/", (req, resp) -> Index.render()); //Index.render() ); // <-- make this work

    }
}

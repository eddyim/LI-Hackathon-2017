package demo;

import bb.sparkjava.BBSparkTemplate;

import static spark.Spark.*;

public class DemoServer {


    public static void main(String[] args) {

        BBSparkTemplate.init();

        get("/", (req, resp) -> "DERP" /* Index.render() */); // <-- make this work
    }
}

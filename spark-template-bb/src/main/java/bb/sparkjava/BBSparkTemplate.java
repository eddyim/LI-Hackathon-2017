package bb.sparkjava;

import bb.runtime.BaseBBTemplate;
import spark.Request;
import spark.Response;

import static spark.Spark.*;

public class BBSparkTemplate extends BaseBBTemplate {

    private static ThreadLocal<Request> REQUEST = new ThreadLocal<Request>();
    private static ThreadLocal<Response> RESPONSE = new ThreadLocal<Response>();


    public Response getResponse() {
        return RESPONSE.get();
    }

    public Request getRequest() {
        return REQUEST.get();
    }

    public static void init() {
        before((request, response) -> {
            REQUEST.set(request);
            RESPONSE.set(response);
        });
        afterAfter((request, response) -> {
            REQUEST.set(null);
            RESPONSE.set(null);
        });
    }
}

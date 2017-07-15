package demo.model;

/**
 * Created by edwardim on 7/15/17.
 */
public class UserRuntimeException extends RuntimeException {
    UserRuntimeException() {
        super();
    }
    UserRuntimeException(String msg) {
        super(msg);
    }
}

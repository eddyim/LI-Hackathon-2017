package bb.runtime;

import java.io.IOException;

public interface ILayout {

    void header(Appendable buffer) throws IOException;

    void footer(Appendable buffer) throws IOException;

}

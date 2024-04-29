package codr7.jalang;

import java.io.IOException;
import java.util.Deque;

public interface Reader {
    boolean read(final Input in, final Deque<Form> out, final Location location) throws IOException;
}

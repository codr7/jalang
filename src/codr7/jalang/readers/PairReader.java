package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;
import codr7.jalang.errors.ReadError;
import codr7.jalang.forms.PairForm;

import java.io.IOException;
import java.util.Deque;

public class PairReader implements Reader {
    public static final PairReader instance = new PairReader();

    public boolean read(final Input in, final Deque<Form> out, final Location location)
            throws IOException {
        final var formLocation = location.clone();

        if (in.peek() != ':') {
            throw new ReadError(location, "Invalid pair.");
        }

        location.update(in.pop());
        final var left = out.removeLast();
        SkipReader.instance.read(in, out, location);

        if (!FormReader.instance.read(in, out, location)) {
            throw new ReadError(location, "Invalid pair.");
        }

        for (; ; ) {
            SkipReader.instance.read(in, out, location);

            if (in.peek() != ':') {
                break;
            }

            read(in, out, location);
        }

        out.addLast(new PairForm(formLocation, left, out.removeLast()));
        return true;
    }
}
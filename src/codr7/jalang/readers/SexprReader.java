package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;
import codr7.jalang.errors.ReadError;
import codr7.jalang.forms.Sexpr;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class SexprReader implements Reader {
  public static final SexprReader instance = new SexprReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();

    if (in.peek() != '(') {
      throw new ReadError(location, "Invalid sexpr.");
    }

    location.update(in.pop());
    SkipReader.instance.read(in, out, location);

    final var body = new ArrayDeque<Form>();

    for (; ; ) {
      SkipReader.instance.read(in, out, location);

      if (in.peek() == ')') {
        location.update(in.pop());
        break;
      }

      if (!FormReader.instance.read(in, body, location)) {
        throw new ReadError(location, "Invalid sexpr.");
      }
    }

    out.addLast(new Sexpr(formLocation, body.toArray(new Form[0])));
    return true;
  }
}
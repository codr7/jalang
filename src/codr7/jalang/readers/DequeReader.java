package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.DequeForm;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class DequeReader implements Reader {
  public static final DequeReader instance = new DequeReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();

    if (in.peek() != '[') {
      throw new ReadError(location, "Invalid deque.");
    }

    location.update(in.pop());
    SkipReader.instance.read(in, out, location);

    final var body = new ArrayDeque<Form>();

    for (; ; ) {
      SkipReader.instance.read(in, out, location);

      if (in.peek() == ']') {
        location.update(in.pop());
        break;
      }

      if (!FormReader.instance.read(in, body, location)) {
        throw new ReadError(location, "Invalid deque.");
      }
    }

    out.addLast(new DequeForm(formLocation, body.toArray(new Form[0])));
    return true;
  }
}

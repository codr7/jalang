package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.Sexpr;
import codr7.jalang.types.Pair;

import java.io.IOException;
import java.util.ArrayDeque;
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
    SkipReader.instance.read(in, out, location);
    final var left = out.removeLast();

    if (!FormReader.instance.read(in, out, location)) {
      throw new ReadError(location, "Invalid pair.");
    }

    for (;;) {
      SkipReader.instance.read(in, out, location);

      if (in.peek() != ':') {
        break;
      }

      read(in, out, location);
    }

    out.addLast(new Pair.Form(formLocation, left, out.removeLast()));
    return true;
  }
}
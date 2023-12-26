package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.Literal;
import codr7.jalang.forms.Sexpr;
import codr7.jalang.libraries.Core;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

public class StringReader implements Reader {
  public static final StringReader instance = new StringReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();

    if (in.peek() != '"') {
      throw new ReadError(location, "Invalid string.");
    }

    location.update(in.pop());
    final var data = new StringBuilder();

    for (;;) {
      if (in.peek() == '"') {
        location.update(in.pop());
        break;
      }

      data.append(location.update(in.pop()));
    }

    out.addLast(new Literal(formLocation, new Value<>(Core.instance.stringType, data.toString())));
    return true;
  }
}

package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;
import codr7.jalang.forms.Identifier;

import java.io.IOException;
import java.util.Deque;

public class IdentifierReader implements Reader {
  public static final IdentifierReader instance = new IdentifierReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();
    final var buffer = new StringBuilder();

    for (; ; ) {
      var c = in.peek();

      if (c == 0 ||
          Character.isWhitespace(c) ||
          c == '(' || c == ')' || c == '[' || c == ']' || c == ':') {
        break;
      }

      in.pop();
      buffer.append(c);
      location.update(c);
    }

    out.addLast(new Identifier(formLocation, buffer.toString()));
    return true;
  }
}
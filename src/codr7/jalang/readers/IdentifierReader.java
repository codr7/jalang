package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.libraries.Core;

import java.io.IOException;
import java.util.Deque;

public class IdentifierReader implements Reader {
  public static final IdentifierReader instance = new IdentifierReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();
    final var buffer = new StringBuilder();

    for (;;) {
      var c = in.peek();

      if (Character.isWhitespace(c) || c == '(' || c == ')') {
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
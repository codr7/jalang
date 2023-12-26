package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.Literal;
import codr7.jalang.libraries.Core;

import java.io.IOException;
import java.util.Deque;

public class IntegerReader implements Reader {
  public static final IntegerReader instance = new IntegerReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();
    int v = 0;

    for (;;) {
      var c = in.peek();

      if (!Character.isDigit(c)) {
        break;
      }

      in.pop();
      v = v * 10 + Character.digit(c, 10);
      location.update(c);
    }

    out.addLast(new Literal(formLocation, new Value<>(Core.instance.integerType, v)));
    return true;
  }
}

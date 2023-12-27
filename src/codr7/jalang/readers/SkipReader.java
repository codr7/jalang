package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;

import java.io.IOException;
import java.util.Deque;

public class SkipReader implements Reader {
  public static final SkipReader instance = new SkipReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    for (; ; ) {
      final var c = in.peek();

      if (Character.isWhitespace(c)) {
        in.pop();
        location.update(c);
      } else {
        break;
      }
    }

    return true;
  }
}

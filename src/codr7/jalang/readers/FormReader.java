package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;

import java.io.IOException;
import java.util.Deque;

public class FormReader implements Reader {
  public static final FormReader instance = new FormReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var c = in.peek();

    switch(c) {
      case 0:
        break;
      default:
        if (Character.isDigit(c)) {
          return IntReader.instance.read(in, out, location);
        }

        if (Character.isWhitespace(c)) {
          SkipReader.instance.read(in, out, location);
          return read(in, out, location);
        }

        return IdentifierReader.instance.read(in, out, location);
    }

    return false;
  }
}

package codr7.jalang.readers;

import codr7.jalang.Form;
import codr7.jalang.Input;
import codr7.jalang.Location;
import codr7.jalang.Reader;
import codr7.jalang.errors.ReadError;
import codr7.jalang.forms.DotForm;
import codr7.jalang.forms.PairForm;

import java.io.IOException;
import java.util.Deque;

public class DotReader implements Reader {
  public static final DotReader instance = new DotReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();

    if (in.peek() != '.') {
      throw new ReadError(location, "Invalid dot.");
    }

    location.update(in.pop());
    SkipReader.instance.read(in, out, location);
    final var left = out.removeLast();

    if (!FormReader.instance.read(in, out, location)) {
      throw new ReadError(location, "Invalid dot.");
    }

    out.addLast(new DotForm(formLocation, left, out.removeLast()));
    return true;
  }
}
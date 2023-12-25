package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.forms.Sexpr;
import codr7.jalang.libraries.Core;

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

    if (!IdentifierReader.instance.read(in, out, location)) {
      throw new ReadError(location, "Missing target.");
    }

    final var target = ((Identifier)out.removeLast()).name();
    final var body = new ArrayDeque<Form>();

    for (;;) {
      SkipReader.instance.read(in, out, location);

      if (in.peek() == ')') {
        location.update(in.pop());
        break;
      }

      if (!FormReader.instance.read(in, body, location)) {
        throw new ReadError(location, "Invalid sexpr.");
      }
    }

    out.addLast(new Sexpr(formLocation, target, body.toArray(new Form[0])));
    return true;
  }
}
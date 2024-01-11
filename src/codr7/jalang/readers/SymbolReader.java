package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.errors.ReadError;
import codr7.jalang.forms.IdForm;
import codr7.jalang.forms.LiteralForm;
import codr7.jalang.libraries.Core;

import java.io.IOException;
import java.util.Deque;

public class SymbolReader implements Reader {
  public static final SymbolReader instance = new SymbolReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();

    if (in.peek() != '\'') {
      throw new ReadError(location, "Invalid symbol.");
    }

    location.update(in.pop());

    if (!IdReader.instance.read(in, out, location)) {
      throw new ReadError(location, "Invalid symbol.");
    }

    final var name = ((IdForm) out.removeLast()).name();
    out.addLast(new LiteralForm(formLocation, new Value<>(Core.symbolType, name)));
    return true;
  }
}

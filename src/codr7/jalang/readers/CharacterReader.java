package codr7.jalang.readers;

import codr7.jalang.*;
import codr7.jalang.errors.ReadError;
import codr7.jalang.forms.LiteralForm;
import codr7.jalang.libraries.Core;

import java.io.IOException;
import java.util.Deque;

public class CharacterReader implements Reader {
  public static final CharacterReader instance = new CharacterReader();

  public boolean read(final Input in, final Deque<Form> out, final Location location)
      throws IOException {
    final var formLocation = location.clone();
    var c = in.peek();

    if (in.peek() != '\\') {
      throw new ReadError(location, "Invalid character.");
    }

    location.update(in.pop());
    c = in.pop();

    out.addLast(new LiteralForm(formLocation, new Value<>(Core.instance.characterType, c)));
    return true;
  }
}

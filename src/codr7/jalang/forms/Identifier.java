package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.operations.Poke;

public class Identifier extends Form {
  public Identifier(final Location location, final String name) {
    super(location);
    this.name = name;
  }

  public void emit(final Vm vm, final Namespace namespace, final int register) {
    final var value = namespace.find(name);

    if (value == null) {
      throw new EmitError(location(), "Unknown identifer: %s", name);
    }

    vm.emit(new Poke(register, value));
  }

  public final String name() {
    return name;
  }

  public String toString() {
    return name;
  }

  private final String name;
}
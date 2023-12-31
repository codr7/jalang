package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.Vm;
import codr7.jalang.errors.EmitError;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Peek;
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

    if (value.type() == Core.instance.registerType) {
      final var source = value.as(Core.instance.registerType).index();

      if (source != register) {
        vm.emit(new Peek(source, register));
      }
    } else {
      vm.emit(new Poke(register, value));
    }
  }

  public final String name() {
    return name;
  }

  public String toString() {
    return name;
  }

  private final String name;
}
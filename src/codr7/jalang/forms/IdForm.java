package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.Vm;
import codr7.jalang.errors.EmitError;

public class IdForm extends Form {
  public IdForm(final Location location, final String name) {
    super(location);
    this.name = name;
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    final var value = namespace.find(name);

    if (value == null) {
      throw new EmitError(location(), "Unknown identifer: %s", name);
    }

    value.emitId(vm, namespace, rResult);
  }

  public final String name() {
    return name;
  }

  public String toString() {
    return name;
  }

  private final String name;
}
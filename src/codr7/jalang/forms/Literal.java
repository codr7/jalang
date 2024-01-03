package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.operations.Poke;

public class Literal extends Form {
  public Literal(final Location location, final Value<?> value) {
    super(location);
    this.value = value;
  }

  public void emit(final Vm vm, final Namespace namespace, final int register) {
    vm.emit(new Poke(value, register));
  }

  public String toString() {
    return value.toString();
  }

  public final Value<?> value() {
    return value;
  }

  private final Value<?> value;
}

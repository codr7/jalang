package codr7.jalang;

import codr7.jalang.operations.Set;

public class Type<D> {
  private final String name;

  public Type(final String name) {
    this.name = name;
  }

  public String dump(final D value) {
    return value.toString();
  }

  public void emitId(final Value<?> value, final Vm vm, final Namespace namespace, final int rResult) {
    vm.emit(new Set(rResult, value));
  }

  public boolean equalValues(final D left, final D right) {
    return left.equals(right);
  }

  public final String name() {
    return name;
  }

  public boolean isTrue(final D value) {
    return true;
  }

  public String say(final D value) {
    return dump(value);
  }

  public final String toString() {
    return name;
  }
}

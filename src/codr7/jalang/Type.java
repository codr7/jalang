package codr7.jalang;

import codr7.jalang.libraries.Core;
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
    if (left == null && right == null) {
      return true;
    }

    if (left == null || right == null) {
      return false;
    }

    return left.equals(right);
  }

  public final String name() {
    return name;
  }

  public boolean isTrue(final D value) {
    return true;
  }

  public Value<?> peek(final Value<?> target) {
    return target;
  }

  public Value<?> pop(final Vm vm, final Value<?> target, final int rTarget) {
    final var result = Core.NONE;
    vm.set(rTarget, result);
    return result;
  }

  public Value<?> push(final Value<?> target, final Value<?> value) {
    return new Value<>(Core.pairType, new Pair(value, target));
  }

  public String say(final D value) {
    return dump(value);
  }

  public final String toString() {
    return name;
  }
}

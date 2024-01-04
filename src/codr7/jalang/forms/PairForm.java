package codr7.jalang.forms;

import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.Vm;
import codr7.jalang.operations.MakePair;

public class PairForm extends codr7.jalang.Form {
  public PairForm(final Location location, final codr7.jalang.Form left, final codr7.jalang.Form right) {
    super(location);
    this.left = left;
    this.right = right;
  }

  public final void emit(final Vm vm, final Namespace namespace, final int register) {
    final var leftRegister = vm.allocateRegister();
    left.emit(vm, namespace, leftRegister);
    final var rightRegister = vm.allocateRegister();
    right.emit(vm, namespace, rightRegister);
    vm.emit(new MakePair(leftRegister, rightRegister, register));
  }

  public final codr7.jalang.Form left() {
    return left;
  }

  public final codr7.jalang.Form right() {
    return right;
  }

  public final String toString() {
    return String.format("%s:%s", left.toString(), right.toString());
  }

  private final codr7.jalang.Form left, right;
}

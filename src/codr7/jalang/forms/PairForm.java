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
    final var rLeft = vm.allocateRegister();
    left.emit(vm, namespace, rLeft);
    final var rRight = vm.allocateRegister();
    right.emit(vm, namespace, rRight);
    vm.emit(new MakePair(rLeft, rRight, register));
    vm.freeRegisters(rLeft);
    vm.freeRegisters(rRight);
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

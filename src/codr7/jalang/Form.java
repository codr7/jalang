package codr7.jalang;

import codr7.jalang.operations.CallIndirect;

public abstract class Form {
  private final Location location;

  public Form(final Location location) {
    this.location = location;
  }

  public final void emitCall(final Vm vm,
                             final Namespace namespace,
                             final Form[] body,
                             final int rResult) {
    final var rTarget = vm.allocateRegister();
    this.emit(vm, namespace, rTarget);
    final var parameters = new int[body.length - 1];

    for (int i = 1; i < body.length; i++) {
      final var rParameter = vm.allocateRegister();
      parameters[i - 1] = rParameter;
      body[i].emit(vm, namespace, rParameter);
    }

    vm.emit(new CallIndirect(location(), rTarget, parameters, rResult));
  }

  public abstract void emit(final Vm vm, final Namespace namespace, final int register);

  public final Location location() {
    return location;
  }

  public abstract String toString();
}

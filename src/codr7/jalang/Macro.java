package codr7.jalang;

import codr7.jalang.forms.RegisterForm;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Return;

public record Macro(String name, int arity, Body body) {
  public void call(final Vm vm,
                   final Namespace namespace,
                   final Location location,
                   final int[] rParameters,
                   final int rResult) {
    final var referenceName = String.format("%s-%d", name, rParameters.length);
    var referenceValue = namespace.find(referenceName);

    if (referenceValue == null) {
      referenceValue = new Value<>(Core.macroReferenceType,
          makeReference(arity, vm, namespace, location, rResult));
      namespace.bind(referenceName, referenceValue);

      if (rParameters.length > 0) {
        vm.reallocateRegisters();
      }
    }

    referenceValue.as(Core.macroReferenceType).call(vm, location, rParameters, rResult);
  }

  public MacroReference makeReference(final int arity,
                                      final Vm vm,
                                      final Namespace namespace,
                                      final Location location,
                                      final int rResult) {
    final var rParameters = new int[arity];
    final var arguments = new Form[arity];

    for (int i = 0; i < rParameters.length; i++) {
      final var r = vm.allocateRegister();
      rParameters[i] = r;
      arguments[i] = new RegisterForm(location, r);
    }

    final var startPc = vm.emitPc();
    emit(vm, namespace, location, arguments, rResult);
    vm.emit(new Return(rResult));
    return new MacroReference(this, startPc, rParameters);
  }

  public void emit(final Vm vm,
                   final Namespace namespace,
                   final Location location,
                   final Form[] arguments,
                   final int rResult) {
    body.call(vm, namespace, location, arguments, rResult);
  }

  public String toString() {
    return String.format("(Macro %s)", name);
  }

  public interface Body {
    void call(final Vm vm,
              final Namespace namespace,
              final Location location,
              final Form[] arguments,
              final int rResult);
  }
}

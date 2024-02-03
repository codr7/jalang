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
          makeReference(vm, namespace, location, rParameters, rResult));
      namespace.bind(referenceName, referenceValue);

      vm.reallocateRegisters();
    }

    referenceValue.as(Core.macroReferenceType).call(vm, location, rParameters, rResult);
  }

  public MacroReference makeReference(final Vm vm,
                                      final Namespace namespace,
                                      final Location location,
                                      final int[] rParameters,
                                      final int rResult) {
    final var arguments = new Form[arity];

    for (int i = 0; i < rParameters.length; i++) {
      arguments[i] = new RegisterForm(location, rParameters[i]);
    }

    final var startPc = vm.emitPc();
    emit(vm, namespace, location, arguments, rResult);
    vm.emit(new Return(rResult));
    return new MacroReference(this.name, startPc, rParameters);
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

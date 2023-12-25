package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Call;
import codr7.jalang.operations.Poke;

public class Sexpr extends Form {
  public Sexpr(final Location location, String targetName, Form...arguments) {
    super(location);
    this.targetName = targetName;
    this.arguments = arguments;
  }

  public void emit(final Vm vm, final Namespace namespace, final int register) {
    final var target = namespace.find(targetName);

    if (target == null) {
      throw new EmitError(location(), "Unknown identifer: %s", targetName);
    }

    if (target.type() == Function.type) {
      final var function = (Function)target.data();

      if (function.arity() != -1 && arguments.length < function.arity()) {
        throw new EmitError(location(), "Not enough arguments.");
      }

      for (int i = 0; i < arguments.length; i++) {
        arguments[i].emit(vm, namespace, i+1);
      }

      vm.emit(new Call(function, arguments.length, register, location()));
    } else {
      throw new EmitError(location(), "Invalid target: %s", target);
    }
  }

  private final String targetName;
  private final Form[] arguments;
}
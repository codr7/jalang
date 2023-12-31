package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.operations.Call;

public class Sexpr extends Form {
  public Sexpr(final Location location, Form... body) {
    super(location);
    this.body = body;
  }

  public void emit(final Vm vm, final Namespace namespace, final int register) {
    final var targetForm = body[0];

    if (!(targetForm instanceof Identifier)) {
      throw new EmitError(location(), "Invalid target: %s", targetForm);
    }

    final var targetName = ((Identifier) targetForm).name();
    final var target = namespace.find(targetName);

    if (target == null) {
      throw new EmitError(location(), "Unknown identifer: %s", targetName);
    }

    final var arity = body.length - 1;

    if (target.type() == Function.type) {
      final var function = (Function) target.data();

      if (function.arity() != -1 && arity < function.arity()) {
        throw new EmitError(location(), "Not enough arguments.");
      }

      for (int i = 1; i < body.length; i++) {
        body[i].emit(vm, namespace, i);
      }

      vm.emit(new Call(function, arity, register, location()));
    } else if (target.type() == Macro.type) {
      final var macro = (Macro) target.data();

      if (macro.arity() != -1 && arity < macro.arity()) {
        throw new EmitError(location(), "Not enough arguments.");
      }

      final Form[] arguments = new Form[arity];

      System.arraycopy(body, 1, arguments, 0, arity);

      macro.call(vm, namespace, location(), arguments, register);
    } else {
      throw new EmitError(location(), "Invalid target: %s", target);
    }
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append('(');

    for (var i = 0; i < body.length; i++) {
      if (i > 0) {
        result.append(' ');
      }

      result.append(body[i].toString());
    }

    result.append(')');
    return result.toString();
  }

  private final Form[] body;
}
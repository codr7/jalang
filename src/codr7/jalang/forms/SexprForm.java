package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.errors.EvaluationError;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.CallIndirect;
import codr7.jalang.operations.Head;
import codr7.jalang.operations.Tail;

public class SexprForm extends Form {
  private final Form[] body;

  public SexprForm(final Location location, Form... body) {
    super(location);
    this.body = body;
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    var targetForm = body[0];
    var head = false;
    var tailCount = 0;

    while (targetForm instanceof PairForm) {
      var pf = (PairForm) targetForm;

      if (pf.left() instanceof NoneForm) {
        targetForm = pf.right();
        tailCount++;
        continue;
      }

      if (pf.right() instanceof NoneForm) {
        head = true;
        targetForm = pf.left();
        break;
      }

      throw new EmitError(location(), "Invalid target: %s.", targetForm);
    }

    Value<?> target = null;

    if (targetForm instanceof IdForm) {
      final var targetName = ((IdForm) targetForm).name();
      target = namespace.find(targetName);

      if (target == null) {
        throw new EmitError(location(), "Unknown identifer: %s", targetName);
      }
    } else if (targetForm instanceof LiteralForm) {
      target = ((LiteralForm) targetForm).value();
    } else {
      final var rTarget = vm.allocateRegister();
      targetForm.emit(vm, namespace, rTarget);
      final var parameters = new int[body.length - 1];

      for (int i = 1; i < body.length; i++) {
        final var rParameter = vm.allocateRegister();
        parameters[i - 1] = rParameter;
        body[i].emit(vm, namespace, rParameter);
      }

      vm.emit(new CallIndirect(location(), rTarget, parameters, rResult));
      return;
    }

    final var arity = body.length - 1;

    if (target.type() == Core.instance.macroType) {
      final var macro = (Macro) target.data();

      if (macro.arity() != -1 && arity < macro.arity()) {
        throw new EmitError(location(), "Not enough arguments.");
      }

      final Form[] arguments = new Form[arity];
      System.arraycopy(body, 1, arguments, 0, arity);
      macro.call(vm, namespace, location(), arguments, rResult);
    } else {
      if (!(target.type() instanceof Core.CallableTrait)) {
        throw new EvaluationError(location(), "Invalid call target: %s.", target);
      }

      final var rParameters = new int[body.length - 1];

      for (int i = 1; i < body.length; i++) {
        final var rParameter = vm.allocateRegister();
        rParameters[i - 1] = rParameter;
        body[i].emit(vm, namespace, rParameter);
      }

      ((Core.CallableTrait) target.type()).emitCall(target, vm, location(), rParameters, rResult);
    }

    if (head) {
      vm.emit(new Head(rResult, rResult));
    } else for (; tailCount > 0; tailCount--) {
      vm.emit(new Tail(rResult, rResult));
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
}
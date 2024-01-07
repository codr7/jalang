package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Set;
import codr7.jalang.operations.Push;

import java.util.ArrayList;

public class VectorForm extends Form {
  public VectorForm(final Location location, Form... body) {
    super(location);
    this.body = body;
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    final var value = new Value<>(Core.instance.vectorType, new ArrayList<>());
    vm.emit(new Set(value, rResult));
    final var rItem = vm.allocateRegister();

    for (final var f : body) {
      f.emit(vm, namespace, rItem);
      vm.emit(new Push(rResult, rItem, rResult));
    }

    vm.freeRegisters(rItem);
  }

  public final Form[] body() {
    return body;
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append('[');

    for (var i = 0; i < body.length; i++) {
      if (i > 0) {
        result.append(' ');
      }

      result.append(body[i].toString());
    }

    result.append(']');
    return result.toString();
  }

  private final Form[] body;
}
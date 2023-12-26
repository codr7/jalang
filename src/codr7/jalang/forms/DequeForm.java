package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.AddLast;
import codr7.jalang.operations.Poke;

import java.util.ArrayDeque;

public class DequeForm extends Form {
  public DequeForm(final Location location, Form...body) {
    super(location);
    this.body = body;
  }

  public void emit(final Vm vm, final Namespace namespace, final int register) {
    vm.emit(new Poke(register, new Value<>(Core.instance.dequeType, new ArrayDeque<>())));
    final var itemRegister = vm.allocateRegister();

    for (final var f: body) {
      f.emit(vm, namespace, itemRegister);
      vm.emit(new AddLast(itemRegister, register));
    }
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
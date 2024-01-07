package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Set;
import codr7.jalang.operations.SetKey;

import java.util.TreeMap;

public class MapForm extends Form {
  public MapForm(final Location location, Form... body) {
    super(location);
    this.body = body;
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    final var value = new Value<>(Core.instance.mapType, new TreeMap<>());
    vm.emit(new Set(value, rResult));
    final var rValue = vm.allocateRegister();

    for (final var f : body) {
      if (f instanceof PairForm) {
        final var pf = (PairForm) f;
        final var rKey = vm.allocateRegister();
        pf.left().emit(vm, namespace, rKey);
        pf.right().emit(vm, namespace, rValue);
        vm.emit(new SetKey(rResult, rKey, rValue));
        vm.freeRegisters(rKey);
      } else {
        f.emit(vm, namespace, rValue);
        vm.emit(new SetKey(rResult, rValue, rValue));
      }
    }
  }

  public final Form[] body() {
    return body;
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append('{');

    for (var i = 0; i < body.length; i++) {
      if (i > 0) {
        result.append(' ');
      }

      result.append(body[i].toString());
    }

    result.append('}');
    return result.toString();
  }

  private final Form[] body;
}

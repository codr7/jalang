package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.Vm;
import codr7.jalang.errors.EmitError;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Peek;
import codr7.jalang.operations.Poke;

public class NoneForm extends Form {
  public NoneForm(final Location location) {
    super(location);
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    vm.emit(new Poke(Core.instance.NONE, rResult));
  }

  public String toString() {
    return "_";
  }
}

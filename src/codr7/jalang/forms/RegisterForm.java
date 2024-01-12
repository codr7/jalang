package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.Vm;
import codr7.jalang.operations.Get;

public class RegisterForm extends Form {
  private final int index;

  public RegisterForm(final Location location, final int index) {
    super(location);
    this.index = index;
  }

  public void emit(final Vm vm, final Namespace namespace, final int rResult) {
    vm.emit(new Get(index, rResult));
  }

  public String toString() {
    return String.format("r%d", index);
  }
}
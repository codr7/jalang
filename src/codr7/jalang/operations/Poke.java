package codr7.jalang.operations;

import codr7.jalang.Operation;
import codr7.jalang.Value;

public class Poke extends Operation {
  public Poke(final int register, Value<?> value) {
    super(Code.Poke);
    this.register = register;
    this.value = value;
  }

  public String toString() {
    return String.format("%s register: %d value: %s", super.toString(), register, value);
  }

  public final int register;
  public final Value<?> value;
}

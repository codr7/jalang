package codr7.jalang.operations;

import codr7.jalang.Operation;

public class EqualsZero extends Operation {
  public EqualsZero(final int value, final int register) {
    super(Code.EqualsZero);
    this.value = value;
    this.register = register;
  }

  public String toString() {
    return String.format("%s value: %d register: %d", super.toString(), value, register);
  }

  public final int value;
  public final int register;
}

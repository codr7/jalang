package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Decrement extends Operation {
  public Decrement(final int register) {
    super(Code.Increment);
    this.register = register;
  }

  public final int register;
}

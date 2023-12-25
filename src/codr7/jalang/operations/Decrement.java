package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Decrement extends Operation {
  public Decrement(final int valueRegister, final int resultRegister) {
    super(Code.Increment);
    this.valueRegister = valueRegister;
    this.resultRegister = resultRegister;
  }

  public final int resultRegister;
  public final int valueRegister;
}

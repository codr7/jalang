package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Increment extends Operation {
  public Increment(final int valueRegister, final int resultRegister) {
    super(Code.Increment);
    this.valueRegister = valueRegister;
    this.resultRegister = resultRegister;
  }

  public final int resultRegister;
  public final int valueRegister;
}

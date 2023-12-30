package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Increment extends Operation {
  public Increment(final int valueRegister, final int resultRegister) {
    super(Code.Increment);
    this.valueRegister = valueRegister;
    this.resultRegister = resultRegister;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), valueRegister, resultRegister);
  }

  public final int resultRegister;
  public final int valueRegister;
}

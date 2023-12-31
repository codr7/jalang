package codr7.jalang.operations;

import codr7.jalang.Operation;
import codr7.jalang.Value;

public class Peek extends Operation {
  public Peek(final int valueRegister, final int resultRegister) {
    super(Code.Peek);
    this.valueRegister = valueRegister;
    this.resultRegister = resultRegister;
  }

  public String toString() {
    return String.format("%s value: %d result: %d",
        super.toString(), valueRegister, resultRegister);
  }

  public final int valueRegister, resultRegister;
}

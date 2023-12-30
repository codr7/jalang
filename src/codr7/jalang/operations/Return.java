package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Return extends Operation {
  public Return(final int resultRegister) {
    super(Code.Return);
    this.resultRegister = resultRegister;

  }

  public String toString() {
    return String.format("%s result: %d", super.toString(), resultRegister);
  }

  public final int resultRegister;
}

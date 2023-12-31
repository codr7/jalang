package codr7.jalang.operations;

import codr7.jalang.Operation;

public class If extends Operation {
  public If(final int conditionRegister, final int elsePc) {
    super(Code.If);
    this.conditionRegister = conditionRegister;
    this.elsePc = elsePc;
  }

  public String toString() {
    return String.format("%s condition: %d else: %d", super.toString(), conditionRegister, elsePc);
  }

  public final int conditionRegister;
  public final int elsePc;
}

package codr7.jalang.operations;

import codr7.jalang.Operation;

public class If extends Operation {
  public If(final int rCondition, final int elsePc) {
    super(Code.If);
    this.rCondition = rCondition;
    this.elsePc = elsePc;
  }

  public String toString() {
    return String.format("%s condition: %d else: %d", super.toString(), rCondition, elsePc);
  }

  public final int rCondition;
  public final int elsePc;
}

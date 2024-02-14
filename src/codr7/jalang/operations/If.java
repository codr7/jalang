package codr7.jalang.operations;

import codr7.jalang.Operation;

public class If extends Operation {
  public final int rCondition;
  public final int elsePc;

  public If(final int rCondition, final int elsePc) {
    super(Code.If);
    this.rCondition = rCondition;
    this.elsePc = elsePc;
  }

  public void addReads(final java.util.Set<Integer> out) {
    out.add(rCondition);
  }

  public String toString() {
    return String.format("%s condition: %d else: %d", super.toString(), rCondition, elsePc);
  }
}

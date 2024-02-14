package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Return extends Operation {
  public final int rResult;

  public Return(final int rResult) {
    super(Code.Return);
    this.rResult = rResult;
  }

  public void addReads(final Set<Integer> out) {
    out.add(rResult);
  }

  public String toString() {
    return String.format("%s result: %d", super.toString(), rResult);
  }
}

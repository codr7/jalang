package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Push extends Operation {
  public final int rResult, rTarget, rValue;

  public Push(final int rTarget, final int rValue, final int rResult) {
    super(Code.Push);
    this.rTarget = rTarget;
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public void addReads(final java.util.Set<Integer> out) {
    out.add(rTarget);
    out.add(rValue);
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }

  public String toString() {
    return String.format("%s target: %d value: %d result: %d",
        super.toString(), rTarget, rValue, rResult);
  }
}

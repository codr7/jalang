package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class BreakPair extends Operation {
  public final int rLeft, rRight, rValue;

  public BreakPair(final int rValue, final int rLeft, final int rRight) {
    super(Code.BreakPair);
    this.rValue = rValue;
    this.rLeft = rLeft;
    this.rRight = rRight;
  }

  public void addReads(final java.util.Set<Integer> out) {
    out.add(rValue);
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rLeft);
    out.add(rRight);
  }

  public String toString() {
    return String.format("%s value: %d left: %d right: %d", super.toString(), rValue, rLeft, rRight);
  }
}

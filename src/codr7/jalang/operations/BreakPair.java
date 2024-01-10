package codr7.jalang.operations;

import codr7.jalang.Operation;

public class BreakPair extends Operation {
  public final int rLeft, rRight, rValue;

  public BreakPair(final int rValue, final int rLeft, final int rRight) {
    super(Code.BreakPair);
    this.rValue = rValue;
    this.rLeft = rLeft;
    this.rRight = rRight;
  }

  public String toString() {
    return String.format("%s value: %d left: %d right: %d", super.toString(), rValue, rLeft, rRight);
  }
}

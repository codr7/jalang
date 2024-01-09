package codr7.jalang.operations;

import codr7.jalang.Operation;

public class MakePair extends Operation {
  public final int rLeft, rRight, rResult;

  public MakePair(final int rLeft, final int rRight, final int rResult) {
    super(Code.MakePair);
    this.rLeft = rLeft;
    this.rRight = rRight;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s left: %d right: %d result: %d", super.toString(), rLeft, rRight, rResult);
  }
}

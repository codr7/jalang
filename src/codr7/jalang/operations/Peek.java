package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Peek extends Operation {
  public Peek(final int rTarget, final int rResult) {
    super(Code.Peek);
    this.rTarget = rTarget;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s target: %d result: %d",
        super.toString(), rTarget, rResult);
  }

  public final int rResult, rTarget;
}


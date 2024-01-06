package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Pop extends Operation {
  public Pop(final int rTarget, final int rResult) {
    super(Code.Pop);
    this.rTarget = rTarget;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s target: %d result: %d",
        super.toString(), rTarget, rResult);
  }

  public final int rResult, rTarget;
}

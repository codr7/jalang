package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Push extends Operation {
  public final int rResult, rTarget, rValue;

  public Push(final int rTarget, final int rValue, final int rResult) {
    super(Code.Push);
    this.rTarget = rTarget;
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s vector: %d value: %d result: %d",
        super.toString(), rTarget, rValue, rResult);
  }
}

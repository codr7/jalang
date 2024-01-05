package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Push extends Operation {
  public Push(final int rVector, final int rValue) {
    super(Code.Push);
    this.rVector = rVector;
    this.rValue = rValue;
  }

  public String toString() {
    return String.format("%s vector: %d value: %d", super.toString(), rVector, rValue);
  }

  public final int rValue, rVector;
}

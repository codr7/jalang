package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Increment extends Operation {
  public final int rResult;
  public final int rValue;

  public Increment(final int rValue, final int rResult) {
    super(Code.Increment);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }
}

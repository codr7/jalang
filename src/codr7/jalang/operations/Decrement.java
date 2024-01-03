package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Decrement extends Operation {
  public Decrement(final int rValue, final int rResult) {
    super(Code.Decrement);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }

  public final int rResult;
  public final int rValue;
}

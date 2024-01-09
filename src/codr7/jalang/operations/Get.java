package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Get extends Operation {
  public final int rValue, rResult;

  public Get(final int rValue, final int rResult) {
    super(Code.Get);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }
}

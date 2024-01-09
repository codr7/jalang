package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Head extends Operation {
  public final int rResult, rValue;

  public Head(final int rValue, final int rResult) {
    super(Code.Head);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }
}

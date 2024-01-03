package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Peek extends Operation {
  public Peek(final int rValue, final int rResult) {
    super(Code.Peek);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }

  public final int rValue, rResult;
}

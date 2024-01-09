package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Tail extends Operation {
  public final int rResult, rValue;

  public Tail(final int rValue, final int rResult) {
    super(Code.Tail);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
  }
}

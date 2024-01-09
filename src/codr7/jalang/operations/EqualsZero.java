package codr7.jalang.operations;

import codr7.jalang.Operation;

public class EqualsZero extends Operation {
  public final int rValue;
  public final int rResult;

  public EqualsZero(final int rValue, final int rResult) {
    super(Code.EqualsZero);
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %d result: %d",
        super.toString(), rValue, rResult);
  }
}

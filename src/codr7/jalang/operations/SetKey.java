package codr7.jalang.operations;

import codr7.jalang.Operation;

public class SetKey extends Operation {
  public SetKey(final int rKey, final int rValue, final int rResult) {
    super(Code.SetKey);
    this.rKey = rKey;
    this.rValue = rValue;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s key: %d value: %d result: %d", super.toString(), rKey, rValue, rResult);
  }

  public final int rKey, rResult, rValue;
}

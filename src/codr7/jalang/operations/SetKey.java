package codr7.jalang.operations;

import codr7.jalang.Operation;

public class SetKey extends Operation {
  public SetKey(final int rMap, final int rKey, final int rValue) {
    super(Code.SetKey);
    this.rMap = rMap;
    this.rKey = rKey;
    this.rValue = rValue;
  }

  public String toString() {
    return String.format("%s map: %d, key: %d value: %d result: %d", super.toString(), rMap, rKey, rValue);
  }

  public final int rKey, rMap, rValue;
}

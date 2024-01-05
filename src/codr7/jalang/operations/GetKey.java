package codr7.jalang.operations;

import codr7.jalang.Operation;

public class GetKey extends Operation {
  public GetKey(final int rMap, final int rKey, final int rResult) {
    super(Code.GetKey);
    this.rMap = rMap;
    this.rKey = rKey;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s key: %d value: %d result: %d", super.toString(), rMap, rKey, rResult);
  }

  public final int rKey, rMap, rResult;
}
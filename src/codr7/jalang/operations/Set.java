package codr7.jalang.operations;

import codr7.jalang.Operation;
import codr7.jalang.Value;

public class Set extends Operation {
  public Set(final Value<?> value, final int rResult) {
    super(Code.Set);
    this.value = value;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s value: %s result: %d", super.toString(), value, rResult);
  }

  public final int rResult;
  public final Value<?> value;
}
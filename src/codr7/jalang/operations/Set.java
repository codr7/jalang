package codr7.jalang.operations;

import codr7.jalang.Operation;
import codr7.jalang.Value;

public class Set extends Operation {
  public final int rResult;
  public final Value<?> value;

  public Set(final int rResult, final Value<?> value) {
    super(Code.Set);
    this.rResult = rResult;
    this.value = value;
  }

  public String toString() {
    return String.format("%s result: %d value: %s", super.toString(), rResult, value);
  }
}

package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Increment extends Operation {
  public Increment(final int register) {
    super(Code.Increment);
    this.register = register;
  }

  public final int register;
}

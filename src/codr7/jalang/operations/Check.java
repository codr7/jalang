package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Check extends Operation {
  public Check(final int expectedRegister, final int actualRegister, final Location location) {
    super(Code.Check);
    this.expectedRegister = expectedRegister;
    this.actualRegister = actualRegister;
    this.location = location;
  }

  public final int actualRegister;
  public final int expectedRegister;
  public final Location location;
}

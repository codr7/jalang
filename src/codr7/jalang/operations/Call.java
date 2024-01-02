package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Call extends Operation {
  public Call(final Function target,
              final int[] parameters,
              final int register,
              final Location location) {
    super(Code.Call);
    this.target = target;
    this.parameters = parameters;
    this.register = register;
    this.location = location;
  }

  public String toString() {
    return String.format("%s target: %s parameters: %s result: %d location: %s",
        super.toString(), target, parameters, register, location);
  }

  public final Location location;
  public final int[] parameters;
  public final int register;
  public final Function target;
}

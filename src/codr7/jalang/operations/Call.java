package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Call extends Operation {
  public Call(final Function target,
              final int[] parameters,
              final int result,
              final Location location) {
    super(Code.Call);
    this.target = target;
    this.parameters = parameters;
    this.result = result;
    this.location = location;
  }

  public String toString() {
    return String.format("%s target: %s parameters: %s result: %d location: %s",
        super.toString(), target, parameters, result, location);
  }

  public final Location location;
  public final int[] parameters;
  public final int result;
  public final Function target;
}

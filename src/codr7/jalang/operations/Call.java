package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Call extends Operation {
  public Call(final Function target,
              final int[] rParameters,
              final int rResult,
              final Location location) {
    super(Code.Call);
    this.target = target;
    this.rParameters = rParameters;
    this.rResult = rResult;
    this.location = location;
  }

  public String toString() {
    return String.format("%s target: %s parameters: %s result: %d location: %s",
        super.toString(), target, rParameters, rResult, location);
  }

  public final Location location;
  public final int[] rParameters;
  public final int rResult;
  public final Function target;
}

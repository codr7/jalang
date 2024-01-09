package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class CallIndirect extends Operation {
  public final Location location;
  public final int[] rParameters;
  public final int rTarget, rResult;
  public CallIndirect(final Location location,
                      final int rTarget,
                      final int[] rParameters,
                      final int rResult) {
    super(Code.CallIndirect);
    this.location = location;
    this.rTarget = rTarget;
    this.rParameters = rParameters;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s target: %d parameters: %s result: %d location: %s",
        super.toString(), rTarget, rParameters, rResult, location);
  }
}

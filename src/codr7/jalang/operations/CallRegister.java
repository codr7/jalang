package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class CallRegister extends Operation {
  public CallRegister(final int rTarget,
                      final int[] rParameters,
                      final int rResult,
                      final Location location) {
    super(Code.CallRegister);
    this.rTarget = rTarget;
    this.rParameters = rParameters;
    this.rResult = rResult;
    this.location = location;
  }

  public String toString() {
    return String.format("%s target: %d parameters: %s result: %d location: %s",
        super.toString(), rTarget, rParameters, rResult, location);
  }

  public final Location location;
  public final int[] rParameters;
  public final int rTarget, rResult;
}

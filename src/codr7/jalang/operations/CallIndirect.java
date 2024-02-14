package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

import java.util.Set;

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

  public void addReads(final java.util.Set<Integer> out) {
    out.add(rTarget);

    for (final var p: rParameters) {
      out.add(p);
    }
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }
  public String toString() {
    return String.format("%s target: %d parameters: %s result: %d location: %s",
        super.toString(), rTarget, rParameters, rResult, location);
  }
}

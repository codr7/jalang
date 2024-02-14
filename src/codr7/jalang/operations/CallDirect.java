package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;
import codr7.jalang.Value;

import java.util.Set;

public class CallDirect extends Operation {
  public final Location location;
  public final Value<?> target;
  public final int[] rParameters;
  public final int rResult;

  public CallDirect(final Location location,
                    final Value<?> target,
                    final int[] rParameters,
                    final int rResult) {
    super(Code.CallDirect);
    this.target = target;
    this.rParameters = rParameters;
    this.rResult = rResult;
    this.location = location;
  }

  public void addReads(final java.util.Set<Integer> out) {
    for (final var p : rParameters) {
      out.add(p);
    }
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }

  public String toString() {
    return String.format("%s target: %s parameters: %s result: %d location: %s",
        super.toString(), target, rParameters, rResult, location);
  }
}

package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class MapIterators extends Operation {
  public MapIterators(final int rFunction,
                      final int[] rIterators,
                      final int[] rValues,
                      final int rResult,
                      final int endPc,
                      final Location location) {
    super(Code.MapIterators);
    this.rFunction = rFunction;
    this.rIterators = rIterators;
    this.rValues = rValues;
    this.rResult = rResult;
    this.endPc = endPc;
    this.location = location;
  }

  public String toString() {
    return String.format("%s function: %d iterators: %s values: %s, result: %d endPc: %d location: %s",
        super.toString(), rFunction, rIterators, rValues, rResult, endPc, location);
  }

  public final int rFunction, rResult;
  public final int[] rIterators, rValues;
  public final int endPc;
  public final Location location;
}

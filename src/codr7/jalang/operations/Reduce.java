package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Reduce extends Operation {
  public Reduce(final int rFunction,
                final int rIterator,
                final int rValue,
                final int rResult,
                final Location location) {
    super(Code.Reduce);
    this.rFunction = rFunction;
    this.rIterator = rIterator;
    this.rValue = rValue;
    this.rResult = rResult;
    this.location = location;
  }

  public String toString() {
    return String.format("%s function: %d iterator: %d value: %d result: %d location: %s",
        super.toString(), rFunction, rIterator, rValue, rResult, location);
  }

  public final int rFunction, rIterator, rValue, rResult;
  public final Location location;
}

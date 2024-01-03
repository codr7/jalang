package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Iterate extends Operation {
  public Iterate(final int rValue, final int rResult, final Location location) {
    super(Code.Iterate);
    this.rValue = rValue;
    this.rResult = rResult;
    this.location = location;
  }

  public String toString() {
    return String.format("%s value: %d result: %d location: %s", super.toString(), rValue, rResult, location);
  }

  public final int rValue, rResult;
  public final Location location;
}
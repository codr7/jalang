package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Iterate extends Operation {
  public final int rIterator, rResult;
  public final int endPc;

  public Iterate(final int rIterator, final int rResult, final int endPc) {
    super(Code.Iterate);
    this.rIterator = rIterator;
    this.rResult = rResult;
    this.endPc = endPc;
  }

  public String toString() {
    return String.format("%s iterator: %d result: %d endPc: %d",
        super.toString(), rIterator, rResult, endPc);
  }
}


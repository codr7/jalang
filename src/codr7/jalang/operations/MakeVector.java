package codr7.jalang.operations;

import codr7.jalang.Operation;

public class MakeVector extends Operation {
  public final int rResult;

  public MakeVector(final int rResult) {
    super(Code.MakeVector);
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s result: %d", super.toString(), rResult);
  }
}


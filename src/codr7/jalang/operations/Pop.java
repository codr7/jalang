package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Pop extends Operation {
  public Pop(final int rVector, final int rResult) {
    super(Code.Pop);
    this.rVector = rVector;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s vector: %d result: %d", super.toString(), rVector, rResult);
  }

  public final int rResult, rVector;
}


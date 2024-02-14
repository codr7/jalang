package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class MakeVector extends Operation {
  public final int rResult;

  public MakeVector(final int rResult) {
    super(Code.MakeVector);
    this.rResult = rResult;
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }

  public String toString() {
    return String.format("%s result: %d", super.toString(), rResult);
  }
}


package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class MakeMap extends Operation {
  public final int rResult;

  public MakeMap(final int rResult) {
    super(Code.MakeMap);
    this.rResult = rResult;
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }


  public String toString() {
    return String.format("%s result: %d", super.toString(), rResult);
  }
}

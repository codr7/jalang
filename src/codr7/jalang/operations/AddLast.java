package codr7.jalang.operations;

import codr7.jalang.Operation;

public class AddLast extends Operation {
  public AddLast(final int rItem, final int rResult) {
    super(Code.AddLast);
    this.rItem = rItem;
    this.rResult = rResult;
  }

  public String toString() {
    return String.format("%s item: %d result: %d", super.toString(), rItem, rResult);
  }

  public final int rItem;
  public final int rResult;
}

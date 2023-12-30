package codr7.jalang.operations;

import codr7.jalang.Operation;

public class AddLast extends Operation {
  public AddLast(final int itemRegister, final int resultRegister) {
    super(Code.AddLast);
    this.itemRegister = itemRegister;
    this.resultRegister = resultRegister;
  }

  public String toString() {
    return String.format("%s item: %d result: %d", super.toString(), itemRegister, resultRegister);
  }

  public final int itemRegister;
  public final int resultRegister;
}

package codr7.jalang.operations;

import codr7.jalang.Operation;

public class MakePair extends Operation {
  public MakePair(final int leftRegister, final int rightRegister, final int resultRegister) {
    super(Code.MakePair);
    this.leftRegister = leftRegister;
    this.rightRegister = rightRegister;
    this.resultRegister = resultRegister;
  }
  public final int leftRegister, rightRegister, resultRegister;
}

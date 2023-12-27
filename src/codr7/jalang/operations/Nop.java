package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Nop extends Operation {
  public static final Nop instance = new Nop();

  public Nop() {
    super(Code.Nop);
  }
}

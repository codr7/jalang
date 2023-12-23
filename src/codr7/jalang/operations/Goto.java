package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Goto extends Operation {
  public Goto(final int pc) {
    super(Code.Goto);
    this.pc = pc;
  }
  public final int pc;
}

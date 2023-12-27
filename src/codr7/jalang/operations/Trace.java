package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Trace extends Operation {
  public static final Trace instance = new Trace();

  public Trace() {
    super(Code.Trace);
  }
}

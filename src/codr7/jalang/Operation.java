package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Goto, Nop, Stop
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public final Code code;
}

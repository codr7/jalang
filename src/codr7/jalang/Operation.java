package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Goto, Nop, MakePair, Poke, Stop, Trace
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public String toString() {
    return code.toString();
  }

  public final Code code;
}

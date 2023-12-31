package codr7.jalang;

public abstract class Operation {
  public enum Code {
    AddLast, Call, Check, Decrement, Goto, If, Increment, MakePair, Nop, Peek, Poke, Return, Stop, Trace
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public String toString() {
    return code.toString();
  }

  public final Code code;
}

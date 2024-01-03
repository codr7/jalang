package codr7.jalang;

public abstract class Operation {
  public enum Code {
    AddLast,
    Benchmark,
    Call, Check,
    Decrement,
    EqualsZero,
    Goto,
    If, Increment, Iterate,
    MakePair,
    Nop,
    Peek, Poke,
    Reduce,
    Return,
    Stop,
    Trace
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public String toString() {
    return code.toString();
  }

  public final Code code;
}

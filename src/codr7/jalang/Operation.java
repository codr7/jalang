package codr7.jalang;

public abstract class Operation {
  public final Code code;

  public Operation(final Code code) {
    this.code = code;
  }

  public String toString() {
    return code.toString();
  }

  public enum Code {
    Benchmark,
    CallDirect, CallIndirect, Check,
    Decrement,
    EqualsZero,
    Get, GetIterator, GetKey, Goto,
    Head,
    If, Increment, Iterate,
    MakePair,
    Nop, NotImplemented,
    Peek, Pop, Push,
    Return,
    Set, SetKey, Stop,
    Tail, Trace
  }
}

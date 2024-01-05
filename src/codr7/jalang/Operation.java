package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Push,
    Benchmark,
    CallDirect, CallIndirect, Check,
    Decrement,
    EqualsZero,
    Get, GetIterator, GetKey, Goto,
    Head,
    If, Increment, Iterate,
    MakePair, MapIterators,
    Nop,
    Pop,
    ReduceIterator, Return,
    Set, SetKey, Stop,
    Tail, Trace
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public String toString() {
    return code.toString();
  }

  public final Code code;
}

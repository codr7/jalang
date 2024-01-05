package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Benchmark,
    CallDirect, CallIndirect, Check,
    Decrement,
    EqualsZero,
    Get, GetIterator, GetKey, Goto,
    Head,
    If, Increment, Iterate,
    MakePair, MapIterators,
    Nop,
    Peek, Pop, Push,
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

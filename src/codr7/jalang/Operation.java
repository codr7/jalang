package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Push,
    Benchmark,
    CallDirect, CallIndirect, Check,
    Decrement,
    EqualsZero,
    GetIterator, GetKey, Goto,
    Head,
    If, Increment, Iterate,
    MakePair, MapIterators,
    Nop,
    Peek, Poke, Pop,
    ReduceIterator, Return,
    SetKey, Stop,
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

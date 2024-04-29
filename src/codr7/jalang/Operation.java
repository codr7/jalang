package codr7.jalang;

import java.util.Set;

public abstract class Operation {
    public final Code code;

    public Operation(final Code code) {
        this.code = code;
    }

    public void addReads(final Set<Integer> out) {
    }

    public void addWrites(final Set<Integer> out) {
    }

    public String toString() {
        return code.toString();
    }

    public enum Code {
        Benchmark, BreakPair,
        CallDirect, CallIndirect, ChangeDirectory, Check,
        Decrement,
        EqualsZero,
        Get, GetIterator, GetKey, Goto,
        If, Increment, Iterate,
        MakeDot, MakeMap, MakePair, MakeVector,
        Nop, NotImplemented,
        Peek, Pop, Push,
        Return,
        Set, SetKey, Stop,
        Tail, Trace
    }
}

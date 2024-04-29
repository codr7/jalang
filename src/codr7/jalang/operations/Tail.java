package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Tail extends Operation {
    public final int rResult, rValue;

    public Tail(final int rValue, final int rResult) {
        super(Code.Tail);
        this.rValue = rValue;
        this.rResult = rResult;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rValue);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s value: %d result: %d", super.toString(), rValue, rResult);
    }
}

package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Iterate extends Operation {
    public final int rIterator, rResult;
    public final int endPc;

    public Iterate(final int rIterator, final int rResult, final int endPc) {
        super(Code.Iterate);
        this.rIterator = rIterator;
        this.rResult = rResult;
        this.endPc = endPc;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rIterator);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s iterator: %d result: %d endPc: %d",
                super.toString(), rIterator, rResult, endPc);
    }
}


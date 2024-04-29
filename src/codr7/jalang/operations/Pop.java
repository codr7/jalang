package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Pop extends Operation {
    public final int rResult, rTarget;

    public Pop(final int rTarget, final int rResult) {
        super(Code.Pop);
        this.rTarget = rTarget;
        this.rResult = rResult;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rTarget);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s target: %d result: %d",
                super.toString(), rTarget, rResult);
    }
}


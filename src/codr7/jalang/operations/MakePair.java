package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class MakePair extends Operation {
    public final int rLeft, rRight, rResult;

    public MakePair(final int rLeft, final int rRight, final int rResult) {
        super(Code.MakePair);
        this.rLeft = rLeft;
        this.rRight = rRight;
        this.rResult = rResult;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rLeft);
        out.add(rRight);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s left: %d right: %d result: %d", super.toString(), rLeft, rRight, rResult);
    }
}

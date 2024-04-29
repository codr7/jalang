package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

import java.util.Set;

public class GetIterator extends Operation {
    public final int rValue, rResult;
    public final Location location;

    public GetIterator(final int rValue, final int rResult, final Location location) {
        super(Code.GetIterator);
        this.rValue = rValue;
        this.rResult = rResult;
        this.location = location;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rValue);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s value: %d result: %d location: %s", super.toString(), rValue, rResult, location);
    }
}
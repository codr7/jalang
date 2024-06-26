package codr7.jalang.operations;

import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Check extends Operation {
    public final int rActual;
    public final int rExpected;
    public final Location location;

    public Check(final int rExpected, final int rActual, final Location location) {
        super(Code.Check);
        this.rExpected = rExpected;
        this.rActual = rActual;
        this.location = location;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rActual);
        out.add(rExpected);
    }

    public String toString() {
        return String.format("%s actual: %d expected: %d location: %s",
                super.toString(), rActual, rExpected, location);
    }
}

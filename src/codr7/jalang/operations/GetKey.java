package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class GetKey extends Operation {
    public final int rKey, rMap, rResult;

    public GetKey(final int rMap, final int rKey, final int rResult) {
        super(Code.GetKey);
        this.rMap = rMap;
        this.rKey = rKey;
        this.rResult = rResult;
    }

    public void addReads(final java.util.Set<Integer> out) {
        out.add(rMap);
        out.add(rKey);
    }

    public void addWrites(final Set<Integer> out) {
        out.add(rResult);
    }

    public String toString() {
        return String.format("%s key: %d value: %d result: %d", super.toString(), rMap, rKey, rResult);
    }
}
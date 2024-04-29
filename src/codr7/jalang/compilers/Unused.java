package codr7.jalang.compilers;

import codr7.jalang.Compiler;
import codr7.jalang.VM;
import codr7.jalang.operations.Goto;
import codr7.jalang.operations.Nop;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class Unused implements Compiler {
    public static final Unused instance = new Unused();

    public boolean compile(final VM vm, final int startPc) {
        var result = false;
        final var writes = new HashMap<Integer, Integer>();

        for (var pc = startPc; pc < vm.code.size(); ) {
            final var op = vm.code.items[pc];
            final var ows = new HashSet<Integer>();
            op.addWrites(ows);

            for (final var r : ows) {
                writes.put(r, pc);
            }

            final var reads = new HashSet<Integer>();
            op.addReads(reads);

            for (final var r : reads) {
                writes.remove(r);
            }

            switch (op.code) {
                case Goto:
                    final var gpc = ((Goto) op).pc;

                    if (gpc > pc) {
                        compile(vm, pc + 1);
                        pc = gpc;
                    } else {
                        pc++;
                    }

                    break;
                case Return:
                    result = result || handleWrites(vm, writes);
                    reads.clear();
                    writes.clear();
                    pc++;
                    break;
                default:
                    pc++;
            }
        }

        return result;
    }

    private boolean handleWrites(final VM vm, final Map<Integer, Integer> writes) {
        if (!writes.isEmpty()) {
            for (final var e : writes.entrySet()) {
                System.out.println("UNUSED " + e);
                vm.code.items[e.getValue()] = Nop.instance;
            }

            return true;
        }

        return false;
    }
}


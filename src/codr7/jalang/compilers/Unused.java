package codr7.jalang.compilers;

import codr7.jalang.Compiler;
import codr7.jalang.Operation;
import codr7.jalang.operations.Goto;
import codr7.jalang.operations.Nop;
import codr7.jalang.operations.Return;

import java.util.*;

public class Unused implements Compiler {
  public static final Unused instance = new Unused();

  public boolean compile(final Operation[] code, final int startPc) {
    var result = false;
    final var writes = new HashMap<Integer, Integer>();

    for (var pc = startPc; pc < code.length;) {
      final var op = code[pc];
      final var ows = new HashSet<Integer>();
      op.addWrites(ows);

      for (final var r: ows) {
        writes.put(r, pc);
      }

      final var reads = new HashSet<Integer>();
      op.addReads(reads);

      for (final var r: reads) {
        writes.remove(r);
      }

      switch (op.code) {
        case Goto:
          final var gpc = ((Goto) op).pc;

          if (gpc > pc) {
            compile(code, pc+1);
            pc = gpc;
          } else {
            pc++;
          }

          break;
        case Return:
          result = result || handleWrites(code, writes);
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

  private boolean handleWrites(final Operation[] code, final Map<Integer, Integer> writes) {
    if (!writes.isEmpty()) {
      for (final var e : writes.entrySet()) {
        System.out.println("UNUSED " + e);
        code[e.getValue()] = Nop.instance;
      }

      return true;
    }

    return false;
  }
}


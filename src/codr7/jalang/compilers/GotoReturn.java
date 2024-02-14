package codr7.jalang.compilers;

import codr7.jalang.Compiler;
import codr7.jalang.Operation;
import codr7.jalang.operations.Goto;
import codr7.jalang.operations.Return;

import java.util.ArrayList;

public class GotoReturn implements Compiler {
  public static final GotoReturn instance = new GotoReturn();

  public boolean compile(final Operation[] code, final int startPc) {
    var result = false;
    final var targetPcs = new ArrayList<Integer>();

    for (var pc = startPc; pc < code.length; ) {
      final var op = code[pc];

      switch (op.code) {
        case Goto:
          final var gpc = ((Goto) op).pc;

          if (gpc > pc) {
            pc = gpc;

            for (final var targetPc : targetPcs) {
              if (pc != ((Goto) code[targetPc]).pc) {
                code[targetPc] = new Goto(pc);
                result = true;
                System.out.println(targetPc + " GOTO");
              }
            }

            targetPcs.add(pc);
          } else {
            targetPcs.clear();
            pc++;
          }

          break;
        case Return:
          for (final var targetPc : targetPcs) {
            code[targetPc] = new Return(((Return) op).rResult);
            result = true;
            System.out.println(targetPc + " RETURN");
          }

          targetPcs.clear();
          pc++;
          break;
        default:
          targetPcs.clear();
          pc++;
      }
    }

    return result;
  }
}

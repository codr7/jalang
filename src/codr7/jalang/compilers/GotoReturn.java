package codr7.jalang.compilers;

import codr7.jalang.Compiler;
import codr7.jalang.VM;
import codr7.jalang.operations.Goto;
import codr7.jalang.operations.Return;

import java.util.ArrayList;

public class GotoReturn implements Compiler {
    public static final GotoReturn instance = new GotoReturn();

    public boolean compile(final VM vm, final int startPc) {
        var result = false;
        final var targetPcs = new ArrayList<Integer>();

        for (var pc = startPc; pc < vm.code.size(); ) {
            final var op = vm.code.items[pc];

            switch (op.code) {
                case Goto:
                    final var gpc = ((Goto) op).pc;

                    if (gpc > pc) {
                        pc = gpc;

                        for (final var targetPc : targetPcs) {
                            if (pc != ((Goto) vm.code.items[targetPc]).pc) {
                                vm.code.items[targetPc] = new Goto(pc);
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
                        vm.code.items[targetPc] = new Return(((Return) op).rResult);
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

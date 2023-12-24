package codr7.jalang;

import java.util.ArrayList;
import java.util.List;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;
import codr7.jalang.types.Pair;

public class Vm {
  public static final int DEFAULT_REGISTER = 0;
  public static final int REGISTER_COUNT = 10;
  public static final int VERSION = 1;

  public Vm() {
    for (var i = 0; i < REGISTER_COUNT; i++) {
      registers.add(null);
    }
  }

  public final int emit(final Operation operation) {
    if (tracingEnabled) { code.add(Trace.instance); }
    final var pc = code.size();
    code.add(operation);
    return pc;
  }

  public final void emit(final int pc, final Operation operation) {
    code.set(pc, operation);
  }

  public final int emitPc() {
    return code.size();
  }

  public final void enableTracing(boolean enabled) {
    tracingEnabled = enabled;
  }

  public final void evaluate(final int startPc) {
    pc = startPc;

    for(;;) {
      final var op = code.get(pc);

      switch (op.code) {
        case Goto:
          pc = ((Goto)op).pc;
          break;
        case MakePair:
          var mpo = (MakePair)op;
          registers.set(mpo.result,
              new Value<Pair>(Core.instance.pairType,
                  new Pair(registers.get(mpo.left), registers.get(mpo.right))));
          pc++;
          break;
        case Nop:
          pc++;
          break;
        case Poke:
          var sro = (Poke)op;
          registers.set(sro.register, sro.value);
          pc++;
          break;
        case Stop:
          pc++;
          return;
        case Trace:
          pc++;
          System.out.printf("%d %s\n", pc, code.get(pc));
          break;
      }
    }
  }

  public Value<?> peek(final int index) {
    return registers.get(index);
  }

  private final List<Operation> code = new ArrayList<>();
  private int pc = -1;
  private final List<Value<?>> registers = new ArrayList<>();
  private boolean tracingEnabled = false;
}

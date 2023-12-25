package codr7.jalang;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;
import codr7.jalang.types.Pair;

import java.util.ArrayList;
import java.util.List;

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
        case Call: {
          final var co = (Call) op;
          co.target.call(this, co.location, co.arity, co.resultRegister);
          pc++;
          break;
        }
        case Decrement: {
          final var o = (Decrement) op;
          final var v = registers.get(o.valueRegister);
          registers.set(o.resultRegister, new Value<>(Core.instance.intType, ((int) v.data()) - 1));
          pc++;
          break;
        }
        case Goto: {
          pc = ((Goto) op).pc;
          break;
        }
        case Increment: {
          final var o = (Increment) op;
          final var v = registers.get(o.valueRegister);
          registers.set(o.resultRegister, new Value<>(Core.instance.intType, ((int) v.data()) + 1));
          pc++;
          break;
        }
        case MakePair: {
          final var o = (MakePair) op;
          registers.set(o.resultRegister,
              new Value<>(Core.instance.pairType,
                  new Pair(registers.get(o.leftRegister), registers.get(o.rightRegister))));
          pc++;
          break;
        }
        case Nop: {
          pc++;
          break;
        }
        case Poke: {
          final var o = (Poke) op;
          registers.set(o.register, o.value);
          pc++;
          break;
        }
        case Stop: {
          pc++;
          return;
        }
        case Trace: {
          pc++;
          System.out.printf("%d %s\n", pc, code.get(pc));
          break;
        }
      }
    }
  }

  public Value<?> peek(final int index) {
    return registers.get(index);
  }

  public void poke(final int index, Value<?> value) {
    registers.set(index, value);
  }

  private final List<Operation> code = new ArrayList<>();
  private int pc = -1;
  private final ArrayList<Value<?>> registers = new ArrayList<>();
  private boolean tracingEnabled = false;
}

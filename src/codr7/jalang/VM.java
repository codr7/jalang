package codr7.jalang;

import java.util.ArrayList;
import java.util.List;

import codr7.jalang.operations.*;

public class VM {
  public final int emit(final Operation operation) {
    if (tracingEnabled) { code.add(Trace.instance); }
    final var pc = code.size();
    code.add(operation);
    return pc;
  }

  public final void emit(final int pc, final Operation operation) {
    code.set(pc, operation);
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
        case Nop:
          pc++;
          break;
        case Stop:
          pc++;
          return;
        case Trace:
          pc++;
          System.out.printf("%d %s", pc, code.get(pc));
          break;
      }
    }
  }
  private boolean tracingEnabled = false;
  private final List<Operation> code = new ArrayList<>();

  private int pc = -1;
}

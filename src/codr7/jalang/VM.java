package codr7.jalang;

import java.util.ArrayList;
import java.util.List;

import codr7.jalang.operations.*;

public class VM {
  public final int emit(final Operation operation) {
    final var pc = code.size();
    code.add(operation);
    return pc;
  }

  public final void emit(final int pc, final Operation operation) {
    code.set(pc, operation);
  }

  public final void evaluate(int pc) {
    for(;;) {
      final var op = code.get(pc);

      switch (op.code) {
        case Goto:
          pc = ((Goto)op).pc;
          break;
        case Nop:
          break;
        case Stop:
          return;
      }
    }
  }
  private final List<Operation> code = new ArrayList<>();
}

package codr7.jalang;

import java.util.ArrayList;
import java.util.List;

public class VM {
  public final int emit(final Operation operation) {
    final var pc = code.size();
    code.add(operation);
    return pc;
  }
  public final void evaluate(int pc) {
    for(;;) {
      final var op = code.get(pc);

      switch (op.code) {
        case Goto:
          pc = ((Operation.Goto)op).pc;
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

package codr7.jalang;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;
import codr7.jalang.readers.FormReader;
import codr7.jalang.types.Pair;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
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

  public final int allocateRegister() {
    final var i = registers.size();
    registers.add(null);
    return i;
  }

  public final int emit(final Operation operation) {
    if (tracingEnabled) {
      code.add(Trace.instance);
    }
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

  public final void toggleTracing() {
    tracingEnabled = !tracingEnabled;
  }

  public final void evaluate(final int startPc) {
    pc = startPc;

    for (; ; ) {
      final var op = code.get(pc);

      switch (op.code) {
        case AddLast: {
          final var o = (AddLast) op;
          final var result = registers.get(o.resultRegister).as(Core.instance.dequeType);
          final var item = registers.get(o.itemRegister);
          result.add(item);
          pc++;
          break;
        }
        case Call: {
          final var o = (Call) op;
          pc++;
          o.target.call(this, o.location, o.arity, o.resultRegister);
          break;
        }
        case Check: {
          final var o = (Check) op;
          final var expected = registers.get(o.expectedRegister);
          evaluate(pc + 1);
          final var actual = registers.get(o.actualRegister);

          if (!actual.equals(expected)) {
            throw new EvaluationError(o.location,
                "Test failed; expected: %s, actual: %s.", expected, actual);
          }

          break;
        }
        case Decrement: {
          final var o = (Decrement) op;
          final var v = registers.get(o.valueRegister);
          registers.set(o.resultRegister,
              new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) - 1));
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
          registers.set(o.resultRegister,
              new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) + 1));
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
        case Return: {
          final var o = (Return) op;
          final var result = registers.get(o.resultRegister);
          registers.clear();
          Collections.addAll(registers, callFrame.registers());
          registers.set(callFrame.resultRegister(), result);
          pc = callFrame.returnPc();
          callFrame = callFrame.parentFrame();
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

  public final void load(final Path path, final Namespace namespace) throws IOException {
    final var p = loadPath.resolve(path);
    final var previousLoadPath = loadPath;
    loadPath = p.getParent();

    try {
      final String code = Files.readString(path);
      final var input = new Input(new StringReader(code));
      final var location = new Location(p.toString());
      final var forms = new ArrayDeque<Form>();
      while (FormReader.instance.read(input, forms, location)) ;

      for (final var f : forms) {
        f.emit(this, namespace, Vm.DEFAULT_REGISTER);
      }
    } finally {
      loadPath = previousLoadPath;
    }
  }

  public final Path loadPath() {
    return loadPath;
  }

  public final Value<?> peek(final int index) {
    return registers.get(index);
  }

  public final void poke(final int index, Value<?> value) {
    registers.set(index, value);
  }

  public final void pushCall(final Function target,
                             final Location location,
                             final int pc,
                             final int resultRegister) {
    callFrame = new CallFrame(
        callFrame,
        target,
        location,
        registers.toArray(new Value<?>[0]),
        this.pc,
        resultRegister);

    this.pc = pc;
  }

  private CallFrame callFrame;
  private final List<Operation> code = new ArrayList<>();
  private Path loadPath = Paths.get("");
  private int pc = -1;
  private final ArrayList<Value<?>> registers = new ArrayList<>();
  private boolean tracingEnabled = false;
}

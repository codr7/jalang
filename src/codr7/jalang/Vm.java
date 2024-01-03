package codr7.jalang;

import codr7.jalang.errors.EvaluationError;
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
          final var result = registers.get(o.rResult).as(Core.instance.dequeType);
          final var item = registers.get(o.rItem);
          result.add(item);
          pc++;
          break;
        }
        case Benchmark: {
          final var o = (Benchmark) op;
          final var t = System.nanoTime();
          final var bodyPc = pc + 1;
          final var repetitions = registers.get(o.rRepetitions).as(Core.instance.integerType);

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc);
          }

          registers.set(o.rRegister,
              new Value<>(Core.instance.floatType, (float) ((System.nanoTime() - t) / 1000000000.0)));
          break;
        }
        case Call: {
          final var o = (Call) op;
          pc++;
          o.target.call(this, o.location, o.rParameters, o.rResult);
          break;
        }
        case Check: {
          final var o = (Check) op;
          final var expected = registers.get(o.rExpected);
          evaluate(pc + 1);
          final var actual = registers.get(o.rActual);

          if (!actual.equals(expected)) {
            throw new EvaluationError(o.location,
                "Test failed; expected: %s, actual: %s.", expected, actual);
          }

          break;
        }
        case Decrement: {
          final var o = (Decrement) op;
          final var v = registers.get(o.rValue);
          final var dv = new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) - 1);
          registers.set(o.rValue, dv);
          registers.set(o.rResult, dv);
          pc++;
          break;
        }
        case EqualsZero: {
          final var o = (EqualsZero) op;
          final var value = registers.get(o.rValue).as(Core.instance.integerType);
          registers.set(o.rResult, new Value<>(Core.instance.bitType, value == 0));
          pc++;
          break;
        }
        case Goto: {
          pc = ((Goto) op).pc;
          break;
        }
        case If: {
          final var o = (If) op;

          if (registers.get(o.rCondition).isTrue()) {
            pc++;
          } else {
            pc = o.elsePc;
          }

          break;
        }
        case Increment: {
          final var o = (Increment) op;
          final var v = registers.get(o.rValue);
          final var iv = new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) + 1);
          registers.set(o.rValue, iv);
          registers.set(o.rResult, iv);
          pc++;
          break;
        }
        case Iterate: {
          final var o = (Iterate) op;
          final var v = registers.get(o.rValue);

          if (!(v.type() instanceof Core.SequenceTrait<?>)) {
            throw new EvaluationError(o.location, "Expected sequence: %s.", v);
          }

          final var i = ((Core.SequenceTrait<Value<?>>)v.type()).iterator(v.data());
          registers.set(o.rResult, new Value<>(Core.instance.iteratorType, i));
          pc++;
          break;
        }
        case MakePair: {
          final var o = (MakePair) op;
          registers.set(o.rResult,
              new Value<>(Core.instance.pairType,
                  new Pair(registers.get(o.rLeft), registers.get(o.rRight))));
          pc++;
          break;
        }
        case Nop: {
          pc++;
          break;
        }
        case Peek: {
          final var o = (Peek) op;
          registers.set(o.rResult, registers.get(o.rValue));
          pc++;
          break;
        }
        case Poke: {
          final var o = (Poke) op;
          registers.set(o.rResult, o.value);
          pc++;
          break;
        }
        case Reduce: {
          final var o = (Reduce) op;
          final var f = registers.get(o.rFunction).as(Function.type);
          final var i = registers.get(o.rIterator).as(Core.instance.iteratorType);
          final var r = registers.get(o.rResult);

          if (i.hasNext()) {
            registers.set(o.rValue, i.next());
            f.call(this, o.location, new int[]{o.rValue, o.rResult}, o.rResult);
          } else {
            pc++;
          }

          break;
        }
        case Return: {
          final var o = (Return) op;
          final var result = registers.get(o.rResult);
          registers.clear();
          Collections.addAll(registers, callFrame.registers());
          registers.set(callFrame.rResult(), result);
          pc = callFrame.returnPc();
          callFrame = callFrame.parentFrame();
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
        default: {
          throw new RuntimeException(String.format("Invalid operation: %s.", op));
        }
      }
    }
  }

  public final void evaluate(final Form form, final Namespace namespace, final int register) {
    final var skipPc = emit(Nop.instance);
    final var startPc = emitPc();
    form.emit(this, namespace, register);
    emit(Stop.instance);
    emit(skipPc, new Goto(emitPc()));
    evaluate(startPc);
  }

  public final void load(final Path path, final Namespace namespace, final int register) throws IOException {
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
        f.emit(this, namespace, register);
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

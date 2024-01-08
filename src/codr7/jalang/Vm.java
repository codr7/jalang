package codr7.jalang;

import codr7.jalang.errors.EvaluationError;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;
import codr7.jalang.readers.FormReader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class Vm {
  public static final int DEFAULT_REGISTER = 0;
  public static final int REGISTER_COUNT = 10;
  public static final int VERSION = 3;

  public final int allocateRegister() {
    final var i = registerCount;
    registerCount++;
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

  public final int emit() {
    return emit(NotImplemented.instance);
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
    if (registers.length != registerCount) {
      registers = Arrays.copyOf(registers, registerCount);
    }

    pc = startPc;

    for (; ; ) {
      final var op = code.get(pc);

      switch (op.code) {
        case Benchmark: {
          final var o = (Benchmark) op;
          final var bodyPc = pc + 1;
          final var repetitions = registers[o.rRepetitions].as(Core.instance.integerType);

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc);
          }

          final var startTime = System.nanoTime();

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc);
          }

          final var elapsedTime = Duration.ofNanos(System.nanoTime() - startTime);
          registers[o.rRegister] = new Value<>(Core.instance.timeType, elapsedTime);
          break;
        }
        case CallDirect: {
          final var o = (CallDirect) op;
          pc++;

          if (!(o.target.type() instanceof Core.CallableTrait)) {
            throw new EvaluationError(o.location, "Invalid call target: %s.", o.target);
          }

          ((Core.CallableTrait) o.target.type()).call(o.target, this, o.location, o.rParameters, o.rResult);
          break;
        }
        case CallIndirect: {
          final var o = (CallIndirect) op;
          final var target = registers[o.rTarget];

          if (!(target.type() instanceof Core.CallableTrait)) {
            throw new EvaluationError(o.location, "Invalid call target: %s.", target);
          }

          pc++;
          ((Core.CallableTrait) target.type()).call(target, this, o.location, o.rParameters, o.rResult);
          break;
        }
        case Check: {
          final var o = (Check) op;
          final var expected = registers[o.rExpected];
          evaluate(pc + 1);
          final var actual = registers[o.rActual];

          if (!actual.equals(expected)) {
            throw new EvaluationError(o.location,
                "Test failed; expected: %s, actual: %s.", expected, actual);
          }

          break;
        }
        case Decrement: {
          final var o = (Decrement) op;
          final var v = registers[o.rValue];
          final var dv = new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) - 1);
          registers[o.rValue] = dv;
          registers[o.rResult] = dv;
          pc++;
          break;
        }
        case EqualsZero: {
          final var o = (EqualsZero) op;
          final var value = registers[o.rValue].as(Core.instance.integerType);
          registers[o.rResult] = new Value<>(Core.instance.bitType, value == 0);
          pc++;
          break;
        }
        case Get: {
          final var o = (Get) op;
          registers[o.rResult] = registers[o.rValue];
          pc++;
          break;
        }
        case GetIterator: {
          final var o = (GetIterator) op;
          final var v = registers[o.rValue];

          if (!(v.type() instanceof Core.SequenceTrait<?>)) {
            throw new EvaluationError(o.location, "Expected sequence: %s.", v);
          }

          final var i = ((Core.SequenceTrait<Value<?>>) v.type()).iterator(v);
          registers[o.rResult] = new Value<>(Core.instance.iteratorType, i);
          pc++;
          break;
        }
        case GetKey: {
          final var o = (GetKey) op;
          final var map = registers[o.rMap].as(Core.instance.mapType);
          final var key = registers[o.rKey];
          registers[o.rResult] = map.get(key);
          pc++;
          break;
        }
        case Goto: {
          pc = ((Goto) op).pc;
          break;
        }
        case Head: {
          final var o = (Head) op;
          registers[o.rResult] = registers[o.rValue].as(Core.instance.pairType).left();
          pc++;
          break;
        }
        case If: {
          final var o = (If) op;

          if (registers[o.rCondition].isTrue()) {
            pc++;
          } else {
            pc = o.elsePc;
          }

          break;
        }
        case Increment: {
          final var o = (Increment) op;
          final var v = registers[o.rValue];
          final var iv = new Value<>(Core.instance.integerType, v.as(Core.instance.integerType) + 1);
          registers[o.rValue] = iv;
          registers[o.rResult] = iv;
          pc++;
          break;
        }
        case Iterate: {
          final var o = (Iterate) op;
          final var it = registers[o.rIterator].as(Core.instance.iteratorType);

          if (it.hasNext()) {
            registers[o.rResult] = it.next();
            pc++;
          } else {
            pc = o.endPc;
          }

          break;
        }
        case MakePair: {
          final var o = (MakePair) op;
          registers[o.rResult] = new Value<>(Core.instance.pairType,
              new Pair(registers[o.rLeft], registers[o.rRight]));
          pc++;
          break;
        }
        case Nop: {
          pc++;
          break;
        }
        case NotImplemented: {
          throw new RuntimeException("Not implemented.");
        }
        case Peek: {
          final var o = (Peek) op;
          final var target = registers[o.rTarget];
          registers[o.rResult] = ((Core.StackTrait) target.type()).peek(this, target);
          pc++;
          break;
        }
        case Pop: {
          final var o = (Pop) op;
          final var target = registers[o.rTarget];
          registers[o.rResult] = ((Core.StackTrait) target.type()).pop(this, target, o.rTarget);
          pc++;
          break;
        }
        case Push: {
          final var o = (Push) op;
          final var target = registers[o.rTarget];
          final var result = ((Core.StackTrait) target.type()).push(target, registers[o.rValue]);
          registers[o.rResult] = result;
          pc++;
          break;
        }
        case Return: {
          final var o = (Return) op;
          final var result = registers[o.rResult];
          registers = callFrame.registers();
          registers[callFrame.rResult()] = result;
          pc = callFrame.returnPc();
          callFrame = callFrame.parentFrame();
          break;
        }
        case Set: {
          final var o = (Set) op;
          registers[o.rResult] = o.value;
          pc++;
          break;
        }
        case SetKey: {
          final var o = (SetKey) op;
          final var map = registers[o.rMap].as(Core.instance.mapType);
          map.put(registers[o.rKey], registers[o.rValue]);
          pc++;
          break;
        }
        case Stop: {
          pc++;
          return;
        }
        case Tail: {
          final var o = (Tail) op;
          registers[o.rResult] = registers[o.rValue].as(Core.instance.pairType).right();
          pc++;
          break;
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
    final var skipPc = emit();
    final var startPc = emitPc();
    form.emit(this, namespace, register);
    emit(Stop.instance);
    emit(skipPc, new Goto(emitPc()));
    evaluate(startPc);
  }

  public final Value<?> get(final int index) {
    return registers[index];
  }

  public final void load(final Path path,
                         final Namespace namespace,
                         final int register) throws IOException {
    final var p = loadPath.resolve(path);
    final var previousLoadPath = loadPath;
    loadPath = p.getParent();

    try {
      final String code = Files.readString(path);
      final var input = new Input(new StringReader(code));
      final var location = new Location(p.toString());
      final var forms = new ArrayDeque<Form>();
      while (FormReader.instance.read(input, forms, location)) ;
      final var startPc = emitPc();

      for (final var f : forms) {
        f.emit(this, namespace, register);
      }

      emit(Stop.instance);
      evaluate(startPc);
    } finally {
      loadPath = previousLoadPath;
    }
  }

  public final Path loadPath() {
    return loadPath;
  }

  public final void pushCall(final Function target,
                             final Location location,
                             final int pc,
                             final int resultRegister) {
    callFrame = new CallFrame(
        callFrame,
        target,
        location,
        Arrays.copyOf(registers, registers.length),
        this.pc,
        resultRegister);

    this.pc = pc;
  }

  public final int registerCount() {
    return registerCount;
  }

  public final void set(final int index, Value<?> value) {
    registers[index] = value;
  }

  private CallFrame callFrame;
  private final ArrayList<Operation> code = new ArrayList<>();
  private Path loadPath = Paths.get("");
  private int pc = -1;
  private Value<?>[] registers = new Value<?>[REGISTER_COUNT];
  private int registerCount = REGISTER_COUNT;
  private boolean tracingEnabled = false;
}

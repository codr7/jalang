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
import java.util.TreeMap;

public class Vm {
  public static final int DEFAULT_REGISTER = 0;
  public static final int VERSION = 6;

  public final Core core = new Core();

  private final ArrayList<Operation> code = new ArrayList<>();
  private CallFrame callFrame;
  private Path loadPath = Paths.get("");
  private int pc = -1;
  private Value<?>[] registers = new Value<?>[1];
  private int registerCount = registers.length;
  private boolean tracingEnabled = false;

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

  public final void evaluate(final int startPc, final Namespace namespace) {
    reallocateRegisters();
    pc = startPc;

    for (; ; ) {
      final var op = code.get(pc);

      switch (op.code) {
        case Benchmark: {
          final var o = (Benchmark) op;
          final var bodyPc = pc + 1;
          final var repetitions = registers[o.rRepetitions].as(Core.integerType);

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc, namespace);
          }

          final var startTime = System.nanoTime();

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc, namespace);
          }

          final var elapsedTime = Duration.ofNanos(System.nanoTime() - startTime);
          registers[o.rRegister] = new Value<>(Core.timeType, elapsedTime);
          break;
        }
        case BreakPair: {
          final var o = (BreakPair) op;
          final var p = registers[((BreakPair) op).rValue].as(Core.pairType);
          registers[o.rLeft] = p.left();
          registers[o.rRight] = p.right();
          pc++;
          break;
        }
        case CallDirect: {
          final var o = (CallDirect) op;
          pc++;

          if (!(o.target.type() instanceof Core.CallableTrait)) {
            throw new EvaluationError(o.location, "Invalid call target: %s.", o.target);
          }

          ((Core.CallableTrait) o.target.type()).call(o.target, this, namespace, o.location, o.rParameters, o.rResult);
          break;
        }
        case CallIndirect: {
          final var o = (CallIndirect) op;
          final var target = registers[o.rTarget];

          if (!(target.type() instanceof Core.CallableTrait)) {
            throw new EvaluationError(o.location, "Invalid call target: %s.", target);
          }

          pc++;
          ((Core.CallableTrait) target.type()).call(target, this, namespace, o.location, o.rParameters, o.rResult);
          break;
        }
        case ChangeDirectory: {
          final var o = (ChangeDirectory) op;
          loadPath = o.path;
          pc++;
          break;
        }
        case Check: {
          final var o = (Check) op;
          final var expected = registers[o.rExpected];
          evaluate(pc + 1, namespace);
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
          final var dv = new Value<>(Core.integerType, v.as(Core.integerType) - 1);
          registers[o.rValue] = dv;
          registers[o.rResult] = dv;
          pc++;
          break;
        }
        case EqualsZero: {
          final var o = (EqualsZero) op;
          final var value = registers[o.rValue].as(Core.integerType);
          registers[o.rResult] = new Value<>(Core.bitType, value == 0);
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
          registers[o.rResult] = new Value<>(Core.iteratorType, i);
          pc++;
          break;
        }
        case GetKey: {
          final var o = (GetKey) op;
          final var map = registers[o.rMap].as(Core.mapType);
          final var key = registers[o.rKey];
          registers[o.rResult] = map.get(key);
          pc++;
          break;
        }
        case Goto: {
          pc = ((Goto) op).pc;
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
          final var iv = new Value<>(Core.integerType, v.as(Core.integerType) + 1);
          registers[o.rValue] = iv;
          registers[o.rResult] = iv;
          pc++;
          break;
        }
        case Iterate: {
          final var o = (Iterate) op;
          final var it = registers[o.rIterator].as(Core.iteratorType);

          if (it.hasNext()) {
            registers[o.rResult] = it.next();
            pc++;
          } else {
            pc = o.endPc;
          }

          break;
        }
        case MakeMap: {
          final var o = (MakeMap) op;
          registers[o.rResult] = new Value<>(Core.mapType, new TreeMap<>());
          pc++;
          break;
        }
        case MakePair: {
          final var o = (MakePair) op;
          registers[o.rResult] = new Value<>(Core.pairType,
              new Pair(registers[o.rLeft], registers[o.rRight]));
          pc++;
          break;
        }
        case MakeVector: {
          final var o = (MakeVector) op;
          registers[o.rResult] = new Value<>(Core.vectorType, new ArrayList<>());
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
          registers[o.rResult] = target.peek();
          pc++;
          break;
        }
        case Pop: {
          final var o = (Pop) op;
          final var target = registers[o.rTarget];
          registers[o.rResult] = target.pop(this, o.rTarget);
          pc++;
          break;
        }
        case Push: {
          final var o = (Push) op;
          final var target = registers[o.rTarget];
          final var result = target.push(registers[o.rValue]);
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
          final var map = registers[o.rMap].as(Core.mapType);
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
          registers[o.rResult] = registers[o.rValue].as(Core.pairType).right();
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
    evaluate(startPc, namespace);
  }

  public final Value<?> get(final int index) {
    return registers[index];
  }

  public final void load(final Path path, final Namespace namespace) throws IOException {
    final var previousLoadPath = loadPath;
    final var p = loadPath.resolve(path);
    var newLoadPath = p.getParent();

    final String code = Files.readString(p);
    final var input = new Input(new StringReader(code));
    final var location = new Location(p.toString());
    final var forms = new ArrayDeque<Form>();
    while (FormReader.instance.read(input, forms, location)) ;
    final var startPc = emitPc();

    if (newLoadPath != null) {
      emit(new ChangeDirectory(newLoadPath));
    }

    for (final var f : forms) {
      f.emit(this, namespace, DEFAULT_REGISTER);
    }

    emit(new ChangeDirectory(previousLoadPath));
  }

  public final Path loadPath() {
    return loadPath;
  }

  public final void pushCall(final Value<?> target,
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

  public void reallocateRegisters() {
    if (registers.length != registerCount) {
      registers = Arrays.copyOf(registers, registerCount);
    }
  }

  public final int registerCount() {
    return registerCount;
  }

  public final void set(final int index, Value<?> value) {
    registers[index] = value;
  }
}

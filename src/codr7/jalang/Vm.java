package codr7.jalang;

import codr7.jalang.errors.EvaluationError;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;
import codr7.jalang.operations.Set;
import codr7.jalang.readers.FormReader;

import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Vm {
  public static final int DEFAULT_REGISTER = 0;
  public static final int REGISTER_COUNT = 10;
  public static final int VERSION = 2;

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
        case Benchmark: {
          final var o = (Benchmark) op;
          final var bodyPc = pc + 1;
          final var repetitions = registers.get(o.rRepetitions).as(Core.instance.integerType);

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc);
          }

          final var t = System.nanoTime();

          for (int i = 0; i < repetitions; i++) {
            evaluate(bodyPc);
          }

          registers.set(o.rRegister,
              new Value<>(Core.instance.floatType, (float) ((System.nanoTime() - t) / 1000000000.0)));
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
          final var target = registers.get(o.rTarget);

          if (!(target.type() instanceof Core.CallableTrait)) {
            throw new EvaluationError(o.location, "Invalid call target: %s.", target);
          }

          pc++;
          ((Core.CallableTrait) target.type()).call(target, this, o.location, o.rParameters, o.rResult);
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
        case Get: {
          final var o = (Get) op;
          registers.set(o.rResult, registers.get(o.rValue));
          pc++;
          break;
        }
        case GetIterator: {
          final var o = (GetIterator) op;
          final var v = registers.get(o.rValue);

          if (!(v.type() instanceof Core.SequenceTrait<?>)) {
            throw new EvaluationError(o.location, "Expected sequence: %s.", v);
          }

          final var i = ((Core.SequenceTrait<Value<?>>) v.type()).iterator(v.data());
          registers.set(o.rResult, new Value<>(Core.instance.iteratorType, i));
          pc++;
          break;
        }
        case GetKey: {
          final var o = (GetKey) op;
          final var map = registers.get(o.rMap).as(Core.instance.mapType);
          final var key = registers.get(o.rKey);
          registers.set(o.rResult, map.get(key));
          pc++;
          break;
        }
        case Goto: {
          pc = ((Goto) op).pc;
          break;
        }
        case Head: {
          final var o = (Head) op;
          registers.set(o.rResult, registers.get(o.rValue).as(Core.instance.pairType).left());
          pc++;
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
          final var it = registers.get(o.rIterator).as(Core.instance.iteratorType);

          if (it.hasNext()) {
            registers.set(o.rResult, it.next());
            pc++;
          } else {
            pc = o.endPc;
          }

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
        case MapIterators: {
          final var o = (MapIterators) op;
          final var function = registers.get(o.rFunction).as(Core.functionType);
          final var iterators = new ArrayList<Iterator<Value<?>>>();

          for (int i = 0; i < o.rIterators.length; i++) {
            iterators.add(registers.get(o.rIterators[i]).as(Core.instance.iteratorType));
          }

          var done = false;

          for (int i = 0; i < o.rIterators.length; i++) {
            final var it = iterators.get(i);

            if (!it.hasNext()) {
              done = true;
              break;
            }

            registers.set(o.rValues[i], it.next());
          }

          if (done) {
            pc = o.endPc;
          } else {
            pc++;
          }

          break;
        }
        case Nop: {
          pc++;
          break;
        }
        case Peek: {
          final var o = (Peek) op;
          final var target = registers.get(o.rTarget);
          registers.set(o.rResult, ((Core.StackTrait)target.type()).peek(this, target));
          pc++;
          break;
        }
        case Pop: {
          final var o = (Pop) op;
          final var target = registers.get(o.rTarget);
          registers.set(o.rResult, ((Core.StackTrait)target.type()).pop(this, target, o.rTarget));
          pc++;
          break;
        }
        case Push: {
          final var o = (Push) op;
          final var target = registers.get(o.rTarget);
          final var result = ((Core.StackTrait)target.type()).push(target, registers.get(o.rValue));
          registers.set(o.rResult, result);
          pc++;
          break;
        }
        case ReduceIterator: {
          final var o = (ReduceIterator) op;
          final var f = registers.get(o.rFunction).as(Core.functionType);
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
        case Set: {
          final var o = (Set) op;
          registers.set(o.rResult, o.value);
          pc++;
          break;
        }
        case SetKey: {
          final var o = (SetKey) op;
          final var map = registers.get(o.rMap).as(Core.instance.mapType);
          map.put(registers.get(o.rKey), registers.get(o.rValue));
          pc++;
          break;
        }
        case Stop: {
          pc++;
          return;
        }
        case Tail: {
          final var o = (Tail) op;
          registers.set(o.rResult, registers.get(o.rValue).as(Core.instance.pairType).right());
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
    final var skipPc = emit(Nop.instance);
    final var startPc = emitPc();
    form.emit(this, namespace, register);
    emit(Stop.instance);
    emit(skipPc, new Goto(emitPc()));
    evaluate(startPc);
  }

  public final Value<?> get(final int index) {
    return registers.get(index);
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
        registers.toArray(new Value<?>[0]),
        this.pc,
        resultRegister);

    this.pc = pc;
  }

  public final void set(final int index, Value<?> value) {
    registers.set(index, value);
  }

  private CallFrame callFrame;
  private final List<Operation> code = new ArrayList<>();
  private Path loadPath = Paths.get("");
  private int pc = -1;
  private final ArrayList<Value<?>> registers = new ArrayList<>();
  private boolean tracingEnabled = false;
}

package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.errors.EvaluationError;
import codr7.jalang.forms.IdForm;
import codr7.jalang.forms.LiteralForm;
import codr7.jalang.forms.PairForm;
import codr7.jalang.forms.VectorForm;
import codr7.jalang.operations.*;
import codr7.jalang.operations.Set;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Core extends Library {
  public interface CallableTrait {
    void call(Value<?> target, Vm vm, Location location, int[] rParameters, int rResult);

    default void emitCall(Value<?> target, Vm vm, Location location, int[] rParameters, int rResult) {
      vm.emit(new CallDirect(location, target, rParameters, rResult));
    }
  }

  public interface CollectionTrait {
    int length(final Value<?> value);
  }

  public interface ComparableTrait {
    Order compare(final Value<?> left, final Value<?> right);
  }

  public interface IndexedCollectionTrait {
    Value<?> slice(final Value<?> value, final Value<?> start, final Value<?> end);
  }

  public interface SequenceTrait<T> {
    Iterator<T> iterator(final Value<?> value);
  }

  public interface StackTrait {
    Value<?> peek(final Vm vm, final Value<?> target);
    Value<?> pop(final Vm vm, final Value<?> target, final int rTarget);
    Value<?> push(final Value<?> target, final Value<?> value);
  }

  public static class BitType extends Type<Boolean> {
    public BitType(final String name) {
      super(name);
    }

    public String dump(final Boolean value) {
      return value ? "T" : "F";
    }

    public boolean isTrue(final Boolean value) {
      return value;
    }
  }

  public static class CharacterType extends Type<Character> implements ComparableTrait {
    public CharacterType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final char l = left.as(this);
      final char r = right.as(this);

      if (l < r) {
        return Order.LessThan;
      }

      if (l > r) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public boolean isTrue(final Character value) {
      return value != 0;
    }
  }

  public static class CollectionType extends Type<Object> {
    public CollectionType(final String name) {
      super(name);
    }
  }

  public static class ComparableType extends Type<Object> {
    public ComparableType(final String name) {
      super(name);
    }
  }

  public static class IndexedCollectionType extends Type<Object> {
    public IndexedCollectionType(final String name) {
      super(name);
    }
  }

  public static class FunctionType extends Type<Function> implements CallableTrait {
    public FunctionType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      target.as(this).call(vm, location, rParameters, rResult);
    }

    public void emitCall(Value<?> target, Vm vm, Location location, int[] rParameters, int rResult) {
        final var function = target.as(this);

        if (function.arity() != -1 && rParameters.length < function.arity()) {
          throw new EmitError(location, "Not enough arguments.");
        }

        CallableTrait.super.emitCall(target, vm, location, rParameters, rResult);
    }

  }

  public static class IntegerType
      extends Type<Integer>
      implements ComparableTrait, SequenceTrait<Value<Integer>> {
    public IntegerType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final int l = left.as(this);
      final int r = right.as(this);

      if (l < r) {
        return Order.LessThan;
      }

      if (l > r) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public boolean isTrue(final Integer value) {
      return value != 0;
    }

    public Iterator<Value<Integer>> iterator(final Value<?> value) {
      return Stream
          .iterate(0, x -> x + 1)
          .limit(value.as(this))
          .map(v -> new Value<>(Core.instance.integerType, v))
          .iterator();
    }
  }

  public static class IteratorType
      extends Type<Iterator<Value<?>>>
      implements SequenceTrait<Value<?>> {
    public IteratorType(final String name) {
      super(name);
    }

    public boolean isTrue(final Iterator<Value<?>> value) {
      return value.hasNext();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Value<?>> iterator(final Value<?> value) {
      return value.as(this);
    }
  }

  public static class MapType
      extends Type<Map<Value<?>, Value<?>>>
      implements CallableTrait, CollectionTrait, SequenceTrait<Value<?>> {
    public MapType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var map = target.as(this);

      switch (rParameters.length) {
        case 1: {
          final var key = vm.get(rParameters[0]);
          final var value = map.get(key);
          vm.set(rResult, (value == null) ? new Value<>(Core.instance.noneType, null) : value);
          break;
        }
        case 2: {
          final var key = vm.get(rParameters[0]);
          final var value = vm.get(rParameters[1]);
          map.put(key, value);
          vm.set(rResult, target);
          break;
        }
        default:
          throw new EvaluationError(location, "Invalid map call.");
      }
    }

    public String dump(final Map<Value<?>, Value<?>> value) {
      final var result = new StringBuilder();
      result.append('{');
      var first = true;

      for (final var e : value.entrySet()) {
        if (!first) {
          result.append(' ');
        }

        if (e.getKey().equals(e.getValue())) {
          result.append(e.getValue().toString());
        } else {
          result.append(e.getKey().toString());
          result.append(':');
          result.append(e.getValue().toString());
        }

        first = false;
      }

      result.append('}');
      return result.toString();
    }

    public boolean equalValues(final Map<Value<?>, Value<?>> left, final Map<Value<?>, Value<?>> right) {
      if (left.size() != right.size()) {
        return false;
      }

      final var li = left.entrySet().iterator();
      final var ri = right.entrySet().iterator();

      while (li.hasNext()) {
        final var le = li.next();
        final var re = ri.next();

        if (!(le.getKey().equals(re.getKey()) && le.getValue().equals(re.getValue()))) {
          return false;
        }
      }

      return true;
    }

    public boolean isTrue(final Map<Value<?>, Value<?>> value) {
      return !value.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Value<?>> iterator(final Value<?> value) {
      final var items = new ArrayList<Value<?>>();

      for (final var e : value.as(this).entrySet()) {
        if (e.getKey().equals(e.getValue())) {
          items.add(e.getKey());
        } else {
          items.add(new Value<>(Core.instance.pairType, new Pair(e.getKey(), e.getValue())));
        }
      }

      return items.iterator();
    }

    @SuppressWarnings("unchecked")
    public int length(final Value<?> value) {
      return value.as(this).size();
    }
  }

  public static class NoneType extends Type<Object> {
    public NoneType(final String name) {
      super(name);
    }

    public String dump(final Object value) {
      return "_";
    }

    public boolean isTrue(final Object value) {
      return false;
    }
  }

  public static class PairType extends Type<Pair> implements StackTrait {
    public PairType(final String name) {
      super(name);
    }

    public String dump(final Pair value) {
      return String.format("%s:%s", value.left().toString(), value.right().toString());
    }

    public boolean isTrue(final Pair value) {
      return value.left().isTrue();
    }

    public Value<?> peek(final Vm vm, final Value<?> target) {
      return target.as(this).left();
    }

    public Value<?> pop(final Vm vm, final Value<?> target, final int rTarget) {
      final var p = target.as(this);
      final var result = p.left();
      vm.set(rTarget, p.right());
      return result;
    }

    public Value<?> push(final Value<?> target, final Value<?> value) {
      return new Value<>(this, new Pair(value, target));
    }
  }

  public static class RegisterType extends Type<Register> implements CallableTrait {
    public RegisterType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var t = vm.get(target.as(this).index());

      if (!(t.type() instanceof Core.CallableTrait)) {
        throw new EvaluationError(location, "Invalid call target: %s.", t);
      }

      ((Core.CallableTrait) t.type()).call(t, vm, location, rParameters, rResult);
    }
  }

  public static class SequenceType extends Type<Object> {
    public SequenceType(final String name) {
      super(name);
    }
  }

  public static class StringType
      extends Type<String>
      implements CollectionTrait, ComparableTrait, IndexedCollectionTrait, SequenceTrait<Value<Character>> {
    public StringType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final var l = left.as(this);
      final var r = right.as(this);
      final var result = l.compareTo(r);

      if (result < 0) {
        return Order.LessThan;
      }

      if (result > 0) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public String dump(final String value) {
      return String.format("\"%s\"", value);
    }

    public boolean isTrue(String value) {
      return !value.isEmpty();
    }

    public Iterator<Value<Character>> iterator(final Value<?> value) {
      return (value.as(this)).codePoints()
          .mapToObj((c) -> new Value<>(Core.instance.characterType, (char) c))
          .iterator();
    }

    public int length(final Value<?> value) {
      return value.as(this).length();
    }

    public String say(final String value) {
      return value;
    }

    public Value<?> slice(final Value<?> value, final Value<?> start, final Value<?> end) {
      final var si = start.as(Core.instance.integerType);
      final var v = value.as(this);

      final var result = (end == null)
          ? v.substring(si)
          : v.substring(si, end.as(Core.instance.integerType));

      return new Value<>(Core.instance.stringType, result);
    }
  }

  public static class SymbolType extends StringType {
    public SymbolType(final String name) {
      super(name);
    }

    public String dump(final String value) {
      return String.format("'%s", value);
    }

    public boolean isTrue(String value) {
      return true;
    }
  }

  public static class TimeType
      extends Type<Duration>
      implements ComparableTrait {
    public TimeType(final String name) {
      super(name);
    }

    public Order compare(final Value<?> left, final Value<?> right) {
      final var l = left.as(this);
      final var r = right.as(this);
      final var result = l.compareTo(r);

      if (result < 0) {
        return Order.LessThan;
      }

      if (result > 0) {
        return Order.GreaterThan;
      }

      return Order.Equal;
    }

    public boolean isTrue(final Duration value) {
      return !value.equals(Duration.ZERO);
    }
  }

  public static class VectorType
      extends Type<ArrayList<Value<?>>>
      implements CallableTrait, CollectionTrait, SequenceTrait<Value<?>>, StackTrait {
    public VectorType(final String name) {
      super(name);
    }

    public void call(final Value<?> target,
                     final Vm vm,
                     final Location location,
                     final int[] rParameters,
                     final int rResult) {
      final var vector = target.as(this);

      switch (rParameters.length) {
        case 1: {
          final var key = vm.get(rParameters[0]);
          final var value = vector.get(key.as(Core.instance.integerType));
          vm.set(rResult, value);
          break;
        }
        case 2: {
          final var key = vm.get(rParameters[0]).as(Core.instance.integerType);
          final var value = vm.get(rParameters[1]);
          vector.set(key, value);
          vm.set(rResult, target);
          break;
        }
        default:
          throw new EvaluationError(location, "Invalid vector call.");
      }
    }

    public String dump(final ArrayList<Value<?>> value) {
      final var result = new StringBuilder();
      result.append('[');
      var first = true;

      for (final var v : value) {
        if (!first) {
          result.append(' ');
        }

        result.append(v.toString());
        first = false;
      }

      result.append(']');
      return result.toString();
    }

    public boolean equalValues(final ArrayList<Value<?>> left, final ArrayList<Value<?>> right) {
      if (left.size() != right.size()) {
        return false;
      }

      final var li = left.iterator();
      final var ri = right.iterator();

      while (li.hasNext()) {
        if (!li.next().equals(ri.next())) {
          return false;
        }
      }
      return true;
    }

    public boolean isTrue(final ArrayList<Value<?>> value) {
      return !value.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Value<?>> iterator(final Value<?> value) {
      return value.as(this).iterator();
    }

    @SuppressWarnings("unchecked")
    public int length(final Value<?> value) {
      return value.as(this).size();
    }

    public Value<?> peek(final Vm vm, final Value<?> target) {
      final var t = target.as(this);
      return t.isEmpty() ? new Value<>(Core.instance.noneType, null) : t.getLast();
    }

    public Value<?> pop(final Vm vm, final Value<?> target, final int rTarget) {
      final var t = target.as(this);
      return t.isEmpty() ? new Value<>(Core.instance.noneType, null) : t.removeLast();
    }

    public Value<?> push(final Value<?> target, final Value<?> value) {
      target.as(this).add(value);
      return target;
    }
  }

  public static final Type<Function> functionType = new FunctionType("Function");
  public static final Type<Macro> macroType = new Type<>("Macro");
  public static final Type<Type<?>> metaType = new Type<>("Meta");

  public static final Core instance = new Core();

  public Core() {
    super("core", null);
    bindType(anyType);
    bindType(bitType);
    bindType(characterType);
    bindType(collectionType);
    bindType(comparableType);
    bindType(functionType);
    bindType(indexedCollectionType);
    bindType(integerType);
    bindType(iteratorType);
    bindType(macroType);
    bindType(mapType);
    bindType(metaType);
    bindType(noneType);
    bindType(pairType);
    bindType(pathType);
    bindType(registerType);
    bindType(sequenceType);
    bindType(stringType);
    bindType(symbolType);
    bindType(timeType);
    bindType(vectorType);

    bind("_", NONE);
    bind("T", T);
    bind("F", F);

    bindFunction("=",
        new Parameter[]{
            new Parameter("value1", anyType),
            new Parameter("value2", anyType)}, 2,
        bitType,
        (function, vm, location, rParameters, rResult) -> {
          final var value1 = vm.get(rParameters[0]);
          var result = true;

          for (int i = 1; i < rParameters.length; i++) {
            if (!vm.get(rParameters[i]).equals(value1)) {
              result = false;
              break;
            }
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction("<",
        new Parameter[]{
            new Parameter("value1", comparableType),
            new Parameter("value2", comparableType)}, 2,
        bitType,
        (function, vm, location, rParameters, rResult) -> {
          var value1 = vm.get(rParameters[0]);
          var type = (ComparableTrait) value1.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var v = vm.get(rParameters[i]);

            if (v.type() != value1.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", value1.type(), v.type());
            }

            if (type.compare(value1, v) != Order.LessThan) {
              result = false;
              break;
            }

            value1 = v;
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction(">",
        new Parameter[]{
            new Parameter("value1", comparableType),
            new Parameter("value2", comparableType)}, 2,
        bitType,
        (function, vm, location, rParameters, rResult) -> {
          var value1 = vm.get(rParameters[0]);
          var type = (ComparableTrait) value1.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var v = vm.get(rParameters[i]);

            if (v.type() != value1.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", value1.type(), v.type());
            }

            if (type.compare(value1, v) != Order.GreaterThan) {
              result = false;
              break;
            }

            value1 = v;
          }

          vm.set(rResult, new Value<>(bitType, result));
        });

    bindFunction("+", new Parameter[]{
            new Parameter("value1", integerType),
            new Parameter("value2", integerType)}, 2,
        integerType,
        (function, vm, location, rParameters, rResult) -> {
          int result = 0;

          for (var i = 0; i < rParameters.length; i++) {
            result += vm.get(rParameters[i]).as(integerType);
          }

          vm.set(rResult, new Value<>(integerType, result));
        });

    bindFunction("-", new Parameter[]{
            new Parameter("value1", integerType),
            new Parameter("value2", integerType)}, 2,
        integerType,
        (function, vm, location, rParameters, rResult) -> {
          int result = vm.get(rParameters[0]).as(integerType);

          if (rParameters.length == 1) {
            result = -result;
          } else {
            for (var i = 1; i < rParameters.length; i++) {
              result -= vm.get(rParameters[i]).as(integerType);
            }
          }

          vm.set(rResult, new Value<>(integerType, result));
        });

    bindMacro("=0", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new EqualsZero(rResult, rResult));
        });

    bindMacro("+1", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var a = arguments[0];
          int rValue;

          if (a instanceof IdForm) {
            final var v = namespace.find(((IdForm) a).name());

            if (v.type() != registerType) {
              throw new EmitError(location, "Invalid target: %s", v.toString());
            }

            final var r = (Register) v.data();

            if (r.type() != null && r.type() != integerType) {
              throw new EmitError(location, "Invalid target: %s", r.type());
            }

            rValue = r.index();
          } else if (a instanceof LiteralForm) {
            rValue = vm.allocateRegister();
            vm.emit(new Set(((LiteralForm) a).value(), rValue));
            vm.freeRegisters(rValue);
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Increment(rValue, rResult));
        });

    bindMacro("-1", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var a = arguments[0];
          int rValue;

          if (a instanceof IdForm) {
            final var v = namespace.find(((IdForm) a).name());

            if (v.type() != registerType) {
              throw new EmitError(location, "Invalid target: %s", v.toString());
            }

            final var r = (Register) v.data();

            if (r.type() != null && r.type() != integerType) {
              throw new EmitError(location, "Invalid target: %s", r.type());
            }

            rValue = r.index();
          } else if (a instanceof LiteralForm) {
            rValue = vm.allocateRegister();
            vm.emit(new Set(((LiteralForm) a).value(), rValue));
            vm.freeRegisters(rValue);
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Decrement(rValue, rResult));
        });

    bindMacro("and", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var andPcs = new ArrayList<Integer>();

          for (int i = 1; i < arguments.length; i++) {
            andPcs.add(vm.emit(Nop.instance));
            arguments[i].emit(vm, namespace, rResult);
          }

          for (final var pc: andPcs) {
            vm.emit(pc, new If(rResult, vm.emitPc()));
          }
        });

    bindMacro("benchmark", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rRepetitions = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rRepetitions);
          vm.emit(new Benchmark(rRepetitions, rResult));

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, rRepetitions);
          }

          vm.freeRegisters(rRepetitions);
          vm.emit(Stop.instance);
        });

    bindMacro("check", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rExpected = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rExpected);
          final var rActual = vm.allocateRegister();
          vm.emit(new Check(rExpected, rActual, location));
          final var bodyNamespace = new Namespace(namespace);

          for (var i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, bodyNamespace, rActual);
          }

          vm.freeRegisters(rExpected);
          vm.freeRegisters(rActual);
          vm.emit(Stop.instance);
        });

    bindMacro("define", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var nameForm = arguments[0];

          if (!(nameForm instanceof IdForm)) {
            throw new EmitError(nameForm.location(), "Expected identifier: %s.", nameForm);
          }
          final var name = ((IdForm) nameForm).name();
          vm.evaluate(arguments[1], namespace, rResult);
          namespace.bind(name, vm.get(rResult));
        });

    bindFunction("digit",
        new Parameter[]{new Parameter("value", characterType)}, 1,
        integerType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]).as(characterType);
          final var result = Character.isDigit(c) ? Character.digit(c, 10) : -1;
          vm.set(rResult, new Value<>(integerType, result));
        });

    bindFunction("digit?",
        new Parameter[]{new Parameter("value", characterType)}, 1,
        bitType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]).as(characterType);
          vm.set(rResult, new Value<>(bitType, Character.isDigit(c)));
        });

    bindMacro("do", 0,
        (vm, namespace, location, arguments, rResult) -> {
          for (final var a : arguments) {
            a.emit(vm, namespace, rResult);
          }
        });

    bindMacro("find", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rPredicate = vm.allocateRegister();
          final var predicateForm = arguments[0];
          predicateForm.emit(vm, namespace, rPredicate);

          final var rIterator = vm.allocateRegister();
          final var inputForm = arguments[1];
          inputForm.emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, inputForm.location()));

          vm.emit(new Set(new Value<>(integerType, -1), rResult));

          final var rIndex = vm.allocateRegister();
          vm.emit(new Set(new Value<>(integerType, 0), rIndex));

          final var iteratePc = vm.emit(Nop.instance);
          final var rValue = vm.allocateRegister();
          final var rPredicateResult = vm.allocateRegister();
          vm.emit(new CallIndirect(location, rPredicate, new int[]{rValue}, rPredicateResult));
          final var ifPc = vm.emit(Nop.instance);
          vm.emit(new MakePair(rValue, rIndex, rResult));
          final var exitPc = vm.emit(Nop.instance);
          vm.emit(ifPc, new If(rPredicateResult, vm.emitPc()));
          vm.emit(new Increment(rIndex, rIndex));
          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
          vm.emit(exitPc, new Goto(vm.emitPc()));
          vm.freeRegisters(rIndex, rPredicate, rPredicateResult, rValue);
        });

    bindMacro("function", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var as = new ArrayDeque<Form>();
          Collections.addAll(as, arguments);
          var name = "";

          if (arguments[0] instanceof IdForm) {
            name = ((IdForm) as.removeFirst()).name();
          }

          var psForm = as.removeFirst();
          Type<?> resultType = null;

          if (psForm instanceof PairForm) {
            var p = (PairForm) psForm;
            var tnf = p.right();
            var tv = namespace.find(((IdForm) tnf).name());

            if (tv == null) {
              throw new EmitError(tnf.location(), "Type not found: %s.", tnf);
            }

            resultType = tv.as(metaType);
            psForm = p.left();
          }

          if (!(psForm instanceof VectorForm)) {
            throw new EmitError(psForm.location(), "Invalid parameter specification: %s.", psForm);
          }

          final var ps = Arrays.stream(((VectorForm) psForm).body()).map((f) -> {
            var pn = "";
            Type<?> pt = anyType;

            if (f instanceof PairForm) {
              final var pf = (PairForm) f;
              if (!((pf.left() instanceof IdForm) && (pf.right() instanceof IdForm))) {
                throw new EmitError(f.location(), "Invalid parameter: %s.", pf);
              }

              final var tf = ((IdForm) pf.left());
              pn = tf.name();
              final var tv = namespace.find(((IdForm) pf.right()).name());

              if (tv == null) {
                throw new EmitError(tf.location(), "Type not found: %s.", pf.right());
              }

              pt = tv.as(metaType);
            } else if (f instanceof IdForm) {
              pn = ((IdForm) f).name();
            } else {
              throw new EmitError(f.location(), "Invalid parameter: %s.", f);
            }

            return new Parameter(pn, pt);
          }).toArray(Parameter[]::new);

          final var skipPc = vm.emit(Nop.instance);
          final var startPc = vm.emitPc();

          final var function = new Function(name, ps, ps.length, resultType,
              (_function, _vm, _location, _parameters, _result) -> {
                _vm.pushCall(_function, _location, startPc, _result);

                for (var i = 0; i < _parameters.length; i++) {
                  vm.set(i + 1, vm.get(_parameters[i]));
                }
              });

          final var v = new Value<>(functionType, function);

          if (!name.isEmpty()) {
            namespace.bind(name, v);
          }

          final var bodyNamespace = new Namespace(namespace);

          for (var i = 0; i < ps.length; i++) {
            final var p = ps[i];
            bodyNamespace.bind(p.name(), new Value<>(registerType, new Register(i + 1, p.type())));
          }

          for (final var f : as) {
            f.emit(vm, bodyNamespace, rResult);
          }

          vm.emit(new Return(rResult));
          vm.emit(skipPc, new Goto(vm.emitPc()));

          if (name.isEmpty()) {
            vm.emit(new Set(v, rResult));
          }
        });

    bindMacro("head", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new Head(rResult, rResult));
        });

    bindMacro("if", 2,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          final var ifPc = vm.emit(Nop.instance);
          arguments[1].emit(vm, namespace, rResult);
          final var skipPc = (arguments.length > 2) ? vm.emit(Nop.instance) : -1;
          vm.emit(ifPc, new If(rResult, vm.emitPc()));

          if (skipPc != -1) {
            arguments[2].emit(vm, namespace, rResult);
            vm.emit(skipPc, new Goto(vm.emitPc()));
          }
        });

    bindFunction("iterator",
        new Parameter[]{new Parameter("sequence", sequenceType)}, 1,
        iteratorType,
        (function, vm, location, rParameters, rResult) -> {
          final var s = vm.get(rParameters[0]);
          @SuppressWarnings("unchecked") final var st = (SequenceTrait<Value<?>>) s.type();
          vm.set(rResult, new Value<>(iteratorType, st.iterator(s)));
        });

    bindFunction("length",
        new Parameter[]{new Parameter("collection", collectionType)}, 1,
        integerType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.get(rParameters[0]);
          final var ct = (CollectionTrait) c.type();
          vm.set(rResult, new Value<>(integerType, ct.length(c)));
        });

    bindMacro("let", 2,
        (vm, namespace, location, arguments, rResult) -> {
      final var bindingsForm = arguments[0];

      if (!(bindingsForm instanceof VectorForm)) {
        throw new EmitError(bindingsForm.location(), "Invalid let bindings: %s.", bindingsForm);
      }

      final var bindings = ((VectorForm)bindingsForm).body();
      final var registers = new ArrayList<Integer>();

      for (int i = 0; i < bindings.length; i += 2) {
        final var nameForm = bindings[i];

        if (!(nameForm instanceof IdForm)) {
          throw new EmitError(nameForm.location(), "Expected identifier: %s.", nameForm);
        }

        if (i == bindings.length - 1) {
          throw new EmitError(bindingsForm.location(), "Missing Value.");
        }

        final var valueForm = bindings[i + 1];
        final var valueType = (valueForm instanceof LiteralForm)
            ? ((LiteralForm)valueForm).value().type()
            : null;
        final var name = ((IdForm)nameForm).name();
        final var rValue = vm.allocateRegister();
        registers.add(rValue);
        namespace.bind(name, new Value<>(registerType, new Register(rValue, valueType)));
        valueForm.emit(vm, namespace, rValue);
      }

      for (int i = 1; i < arguments.length; i++) {
        arguments[i].emit(vm, namespace, rResult);
      }

      for (final var r: registers) {
        vm.freeRegisters(r);
      }
    });

    bindMacro("load", 1,
        (vm, namespace, location, arguments, rResult) -> {
          vm.evaluate(arguments[0], namespace, rResult);
          final var path = vm.get(rResult).as(pathType);
          vm.set(rResult, null);

          try {
            vm.load(path, namespace, rResult);
          } catch (final IOException e) {
            throw new EmitError(location, e.toString());
          }
        });


    bindMacro("map", 2,
        (vm, namespace, location, arguments, rResult) -> {
          final var rFunction = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rFunction);
          final var rIterators = new int[arguments.length - 1];
          final var rValues = new int[rIterators.length];

          for (int i = 0; i < rIterators.length; i++) {
            final var r = vm.allocateRegister();
            final var f = arguments[i + 1];
            f.emit(vm, namespace, r);
            vm.emit(new GetIterator(r, r, f.location()));
            rIterators[i] = r;
            rValues[i] = vm.allocateRegister();
          }

          final var rCall = vm.allocateRegister();
          vm.emit(new Set(new Value<>(Core.instance.vectorType, new ArrayList<>()), rResult));
          final var mapPc = vm.emit(Nop.instance);
          vm.emit(new CallIndirect(location, rFunction, rValues, rCall));
          vm.emit(new Push(rResult, rCall, rResult));
          vm.emit(new Goto(mapPc));
          vm.emit(mapPc, new MapIterators(rFunction, rIterators, rValues, rResult, vm.emitPc(), location));
          vm.emit(new GetIterator(rResult, rResult, location));
        });

    bindFunction("not",
        new Parameter[]{new Parameter("value", anyType)}, 1,
        bitType,
        (function, vm, location, rParameters, rResult) -> {
      vm.set(rResult, new Value<>(bitType, !vm.get(rParameters[0]).isTrue()));
        });

          bindMacro("or", 2,
        (vm, namespace, location, arguments, rResult) -> {
        arguments[0].emit(vm, namespace, rResult);
        final var skipPcs = new ArrayList<Integer>();

        for (int i = 1; i < arguments.length; i++) {
          final var orPc = vm.emit(Nop.instance);
          skipPcs.add(vm.emit(Nop.instance));
          vm.emit(orPc, new If(rResult, vm.emitPc()));
          arguments[i].emit(vm, namespace, rResult);
        }

        for (final var pc: skipPcs) {
          vm.emit(pc, new Goto(vm.emitPc()));
        }
    });

    bindFunction("parse-integer",
        new Parameter[]{new Parameter("input", stringType)}, 1,
        pairType,
        (function, vm, location, rParameters, rResult) -> {
          final var input = vm.get(rParameters[0]).as(stringType);
          final var match = Pattern.compile("^\\s*(\\d+).*").matcher(input);

          if (!match.find()) {
            throw new EvaluationError(location, "Invalid integer: %s", input);
          }

          vm.set(rResult, new Value<>(pairType, new Pair(
              new Value<>(integerType, Integer.valueOf(match.group(1))),
              new Value<>(integerType, match.end(1)))));
        });

    bindFunction("path",
        new Parameter[]{new Parameter("value", stringType)}, 1,
        pathType,
        (function, vm, location, rParameters, rResult) -> {
          vm.set(rResult, new Value<>(pathType, Paths.get(vm.get(rParameters[0]).as(stringType))));
        });

    bindMacro("peek", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rTarget = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rTarget);
          vm.emit(new Peek(rTarget, rResult));
          vm.freeRegisters(rTarget);
        });

    bindMacro("pop", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var rTarget = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rTarget);
          vm.emit(new Pop(rTarget, rResult));
          vm.freeRegisters(rTarget);
        });

    bindMacro("push", 2,
        (vm, namespace, location, arguments, rResult) -> {
      arguments[0].emit(vm, namespace, rResult);
      final var rValue = vm.allocateRegister();
      arguments[1].emit(vm, namespace, rValue);
      vm.emit(new Push(rResult, rValue, rResult));
      vm.freeRegisters(rValue);
    });

    bindMacro("reduce", 3,
        (vm, namespace, location, arguments, rResult) -> {
          final var rFunction = vm.allocateRegister();
          arguments[0].emit(vm, namespace, rFunction);
          final var rIterator = vm.allocateRegister();
          final var f = arguments[1];
          f.emit(vm, namespace, rIterator);
          vm.emit(new GetIterator(rIterator, rIterator, f.location()));
          final var rValue = vm.allocateRegister();
          arguments[2].emit(vm, namespace, rResult);
          vm.emit(new ReduceIterator(rFunction, rIterator, rValue, rResult, location));
        });

    bindFunction("register-count",
        new Parameter[]{}, 0,
        integerType,
        (function, vm, location, rParameters, rResult) -> {
      vm.set(rResult, new Value<>(integerType, vm.registerCount()));
        });

          bindFunction("say",
        new Parameter[]{new Parameter("value1", anyType)}, 1,
        noneType,
        (function, vm, location, rParameters, rResult) -> {
          final var what = new StringBuilder();

          for (var i = 0; i < rParameters.length; i++) {
            if (i > 0) {
              what.append(' ');
            }

            what.append(vm.get(rParameters[i]).say());
          }

          System.out.println(what);
          System.out.flush();
        });

    bindFunction("slice",
        new Parameter[]{
            new Parameter("input", indexedCollectionType),
            new Parameter("start", anyType),
            new Parameter("end", anyType)
        }, 2,
        indexedCollectionType,
        (function, vm, location, rParameters, rResult) -> {
          final var i = vm.get(rParameters[0]);
          final var it = (IndexedCollectionTrait) i.type();
          final var start = vm.get(rParameters[1]);
          final var end = (rParameters.length == 2) ? null : vm.get(rParameters[2]);
          vm.set(rResult, it.slice(i, start, end));
        });

    bindFunction("string",
        new Parameter[]{new Parameter("value1", anyType)}, 1,
        stringType,
        (function, vm, location, rParameters, rResult) -> {
          final var result = new StringBuilder();

          for (var i = 0; i < rParameters.length; i++) {
            result.append(vm.get(rParameters[i]).say());
          }

          vm.set(rResult, new Value<>(stringType, result.toString()));
        });

    bindFunction("reverse-string",
        new Parameter[]{new Parameter("input", stringType)}, 1,
        stringType,
        (function, vm, location, rParameters, rResult) -> {
          final var result = new StringBuilder(vm.get(rParameters[0]).as(stringType)).reverse().toString();
          vm.set(rResult, new Value<>(stringType, result));
        });

    bindFunction("slurp",
        new Parameter[]{new Parameter("path", pathType)}, 1,
        stringType,
        (function, vm, location, rParameters, rResult) -> {
          try {
            final var p = vm.loadPath().resolve(vm.get(rParameters[0]).as(pathType));
            final String data = Files.readString(p);
            vm.set(rResult, new Value<>(stringType, data));
          } catch (final IOException e) {
            throw new EvaluationError(location, "Failed reading file: %s", e);
          }
        });

    bindFunction("split",
        new Parameter[]{
            new Parameter("whole", stringType),
            new Parameter("separator", stringType)}, 2,
        iteratorType,
        (function, vm, location, rParameters, rResult) -> {
          final var w = vm.get(rParameters[0]).as(stringType);
          final var s = vm.get(rParameters[1]).as(stringType);
          final String[] parts = w.split(Pattern.quote(s));
          final var result = new ArrayList<Value<?>>();

          for (final var p : parts) {
            result.add(new Value<>(stringType, p));
          }

          vm.set(rResult, new Value<>(iteratorType, result.iterator()));
        });

    bindMacro("tail", 1,
        (vm, namespace, location, arguments, rResult) -> {
          arguments[0].emit(vm, namespace, rResult);
          vm.emit(new Tail(rResult, rResult));
        });

    bindMacro("trace", 0,
        (vm, namespace, location, rParameters, rResult) -> {
          vm.toggleTracing();
        });

    bindFunction("vector",
        new Parameter[]{new Parameter("input", sequenceType)}, 1,
        vectorType,
        (function, vm, location, rParameters, rResult) -> {
          final var input = vm.get(rParameters[0]);

          @SuppressWarnings("unchecked")
          final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input);
          final var result = new ArrayList<Value<?>>();

          while (iterator.hasNext()) {
            result.add(iterator.next());
          }

          vm.set(rResult, new Value<>(vectorType, result));
        });
  }

  public final Type<Object> anyType = new Type<>("Any");
  public final BitType bitType = new BitType("Bit");
  public final Value<Boolean> T = new Value<>(bitType, true);
  public final Value<Boolean> F = new Value<>(bitType, false);
  public final CharacterType characterType = new CharacterType("Character");
  public final CollectionType collectionType = new CollectionType("Collection");
  public final ComparableType comparableType = new ComparableType("Comparable");
  public final IndexedCollectionType indexedCollectionType = new IndexedCollectionType("IndexedCollection");
  public final IntegerType integerType = new IntegerType("Integer");
  public final IteratorType iteratorType = new IteratorType("Iterator");
  public final MapType mapType = new MapType("Map");
  public final NoneType noneType = new NoneType("None");
  public final Value<Object> NONE = new Value<>(noneType, null);
  public final PairType pairType = new PairType("Pair");
  public final Type<Path> pathType = new Type<>("Path");
  public final RegisterType registerType = new RegisterType("Register");
  public final SequenceType sequenceType = new SequenceType("Sequence");
  public final StringType stringType = new StringType("String");
  public final SymbolType symbolType = new SymbolType("Symbol");
  public final TimeType timeType = new TimeType("Time");
  public final VectorType vectorType = new VectorType("Vector");
}

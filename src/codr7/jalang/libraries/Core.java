package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.errors.EvaluationError;
import codr7.jalang.forms.DequeForm;
import codr7.jalang.forms.IdForm;
import codr7.jalang.forms.LiteralForm;
import codr7.jalang.forms.PairForm;
import codr7.jalang.operations.*;
import codr7.jalang.Compare;
import codr7.jalang.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Core extends Library {
  public interface CollectionTrait {
    int length(final Object value);
  }

  public interface ComparableTrait {
    Compare compare(final Object left, final Object right);
  }

  public interface IndexedCollectionTrait {
    Value<?> slice(final Object value, final int start);
  }

  public interface SequenceTrait<T> {
    Iterator<T> iterator(final Object value);
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

    public Compare compare(final Object left, final Object right) {
      final char l = (Character) left;
      final char r = (Character) right;

      if (l < r) {
        return Compare.LessThan;
      }

      if (l > r) {
        return Compare.GreaterThan;
      }

      return Compare.Equal;
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

  public static class DequeType
      extends Type<Deque<Value<?>>>
      implements CollectionTrait, SequenceTrait<Value<?>> {
    public DequeType(final String name) {
      super(name);
    }

    public String dump(final Deque<Value<?>> value) {
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

    public boolean equalValues(final Deque<Value<?>> left, final Deque<Value<?>> right) {
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

    public boolean isTrue(final Deque<Value<?>> value) {
      return !value.isEmpty();
    }

    @SuppressWarnings("unchecked")
    public Iterator<Value<?>> iterator(final Object value) {
      return ((Deque<Value<?>>) value).iterator();
    }

    @SuppressWarnings("unchecked")
    public int length(final Object value) {
      return ((Deque<Value<?>>) value).size();
    }
  }

  public static class FloatType
      extends Type<Float>
      implements ComparableTrait {
    public FloatType(final String name) {
      super(name);
    }

    public Compare compare(final Object left, final Object right) {
      final float l = (Float) left;
      final float r = (Float) right;

      if (l < r) {
        return Compare.LessThan;
      }

      if (l > r) {
        return Compare.GreaterThan;
      }

      return Compare.Equal;
    }

    public boolean isTrue(final Float value) {
      return value != 0;
    }
  }

  public static class IndexedCollectionType extends Type<Object> {
    public IndexedCollectionType(final String name) {
      super(name);
    }
  }

  public static class IntegerType
      extends Type<Integer>
      implements ComparableTrait, SequenceTrait<Value<Integer>> {
    public IntegerType(final String name) {
      super(name);
    }

    public Compare compare(final Object left, final Object right) {
      final int l = (Integer) left;
      final int r = (Integer) right;

      if (l < r) {
        return Compare.LessThan;
      }

      if (l > r) {
        return Compare.GreaterThan;
      }

      return Compare.Equal;
    }

    public boolean isTrue(final Integer value) {
      return value != 0;
    }

    public Iterator<Value<Integer>> iterator(final Object value) {
      return Stream
          .iterate(0, x -> x + 1)
          .limit((Integer) value)
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
    public Iterator<Value<?>> iterator(final Object value) {
      return (Iterator<Value<?>>) value;
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

  public static class PairType extends Type<Pair> {
    public PairType(final String name) {
      super(name);
    }

    public String dump(final Pair value) {
      return String.format("%s:%s", value.left().toString(), value.right().toString());
    }

    public boolean isTrue(final Pair value) {
      return value.left().isTrue();
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

    public Compare compare(final Object left, final Object right) {
      final var l = (String) left;
      final var r = (String) right;
      final var result = l.compareTo(r);

      if (result < 0) {
        return Compare.LessThan;
      }

      if (result > 0) {
        return Compare.GreaterThan;
      }

      return Compare.Equal;
    }

    public String dump(final String value) {
      return String.format("\"%s\"", value);
    }

    public boolean isTrue(String value) {
      return !value.isEmpty();
    }

    public Iterator<Value<Character>> iterator(final Object value) {
      return ((String) value).codePoints()
          .mapToObj((c) -> new Value<>(Core.instance.characterType, (char) c))
          .iterator();
    }

    public int length(final Object value) {
      return ((String) value).length();
    }

    public String say(final String value) {
      return value;
    }

    public Value<?> slice(final Object value, final int start) {
      return new Value<>(Core.instance.stringType, ((String) value).substring(start));
    }
  }

  public static final Core instance = new Core();

  public Core() {
    super("core", null);
    bindType(bitType);
    bindType(characterType);
    bindType(collectionType);
    bindType(comparableType);
    bindType(dequeType);
    bindType(floatType);
    bindType(Function.type);
    bindType(indexedCollectionType);
    bindType(integerType);
    bindType(iteratorType);
    bindType(Macro.type);
    bindType(noneType);
    bindType(Type.meta);
    bindType(pairType);
    bindType(pathType);
    bindType(registerType);
    bindType(sequenceType);
    bindType(stringType);

    bind("_", NONE);
    bind("T", T);
    bind("F", F);

    bindFunction("=",
        new Parameter[]{new Parameter("left", anyType),
            new Parameter("right", anyType)}, bitType,
        (function, vm, location, rParameters, rResult) -> {
          final var result = vm.peek(rParameters[0]).equals(vm.peek(rParameters[1]));
          vm.poke(rResult, new Value<>(bitType, result));
        });

    bindFunction("<",
        new Parameter[]{new Parameter("value1", comparableType)}, bitType,
        (function, vm, location, rParameters, rResult) -> {
          var left = vm.peek(rParameters[0]);
          var type = (ComparableTrait) left.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var right = vm.peek(rParameters[i]);

            if (right.type() != left.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", left.type(), right.type());
            }

            if (type.compare(left.data(), right.data()) != Compare.LessThan) {
              result = false;
              break;
            }

            left = right;
          }

          vm.poke(rResult, new Value<>(bitType, result));
        });

    bindFunction(">",
        new Parameter[]{new Parameter("value1", comparableType)}, bitType,
        (function, vm, location, rParameters, rResult) -> {
          var left = vm.peek(rParameters[0]);
          var type = (ComparableTrait) left.type();
          var result = true;

          for (var i = 1; i < rParameters.length; i++) {
            final var right = vm.peek(rParameters[i]);

            if (right.type() != left.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", left.type(), right.type());
            }

            if (type.compare(left.data(), right.data()) != Compare.GreaterThan) {
              result = false;
              break;
            }

            left = right;
          }

          vm.poke(rResult, new Value<>(bitType, result));
        });

    bindFunction("+", null, integerType,
        (function, vm, location, rParameters, rResult) -> {
          int result = 0;

          for (var i = 0; i < rParameters.length; i++) {
            result += vm.peek(rParameters[i]).as(integerType);
          }

          vm.poke(rResult, new Value<>(integerType, result));
        });

    bindFunction("-", null, integerType,
        (function, vm, location, rParameters, rResult) -> {
          int result = vm.peek(rParameters[0]).as(integerType);

          if (rParameters.length == 1) {
            result = -result;
          } else {
            for (var i = 1; i < rParameters.length; i++) {
              result -= vm.peek(rParameters[i]).as(integerType);
            }
          }

          vm.poke(rResult, new Value<>(integerType, result));
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
            vm.poke(rValue, ((LiteralForm) a).value());
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
            vm.poke(rValue, ((LiteralForm) a).value());
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Decrement(rValue, rResult));
        });

    bindMacro("benchmark", 1,
        (vm, namespace, location, arguments, rResult) -> {
          final var repetitions = vm.allocateRegister();
          arguments[0].emit(vm, namespace, repetitions);
          vm.emit(new Benchmark(repetitions, rResult));

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, repetitions);
          }

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

          vm.emit(Stop.instance);
        });

    bindFunction("deque",
        new Parameter[]{new Parameter("input", sequenceType)}, dequeType,
        (function, vm, location, rParameters, rResult) -> {
          final var input = vm.peek(rParameters[0]);

          @SuppressWarnings("unchecked") final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input.data());
          final var result = new ArrayDeque<Value<?>>();

          while (iterator.hasNext()) {
            result.add(iterator.next());
          }

          vm.poke(rResult, new Value<>(dequeType, result));
        });

    bindFunction("digit",
        new Parameter[]{new Parameter("value", characterType)}, integerType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.peek(rParameters[0]).as(characterType);
          final var result = Character.isDigit(c) ? Character.digit(c, 10) : -1;
          vm.poke(rResult, new Value<>(integerType, result));
        });

    bindFunction("digit?",
        new Parameter[]{new Parameter("value", characterType)}, bitType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.peek(rParameters[0]).as(characterType);
          vm.poke(rResult, new Value<>(bitType, Character.isDigit(c)));
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

          vm.emit(new Poke(new Value<>(integerType, -1), rResult));

          final var rIndex = vm.allocateRegister();
          vm.emit(new Poke(new Value<>(integerType, 0), rIndex));

          final var iteratePc = vm.emit(Nop.instance);
          final var rValue = vm.allocateRegister();
          final var rPredicateResult = vm.allocateRegister();
          vm.emit(new CallRegister(rPredicate, new int[]{rValue}, rPredicateResult, location));
          final var ifPc = vm.emit(Nop.instance);
          vm.emit(new MakePair(rValue, rIndex, rResult));
          final var exitPc = vm.emit(Nop.instance);
          vm.emit(ifPc, new If(rPredicateResult, vm.emitPc()));
          vm.emit(new Increment(rIndex, rIndex));
          vm.emit(new Goto(iteratePc));
          vm.emit(iteratePc, new Iterate(rIterator, rValue, vm.emitPc()));
          vm.emit(exitPc, new Goto(vm.emitPc()));
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

            resultType = tv.as(Type.meta);
            psForm = p.left();
          }

          if (!(psForm instanceof DequeForm)) {
            throw new EmitError(psForm.location(), "Invalid parameter specification: %s.", psForm);
          }

          final var ps = Arrays.stream(((DequeForm) psForm).body()).map((f) -> {
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
                throw new EmitError(tf.location(), "Type not found: %s.", tf);
              }

              pt = tv.as(Type.meta);
            } else if (f instanceof IdForm) {
              pn = ((IdForm) f).name();
            } else {
              throw new EmitError(f.location(), "Invalid parameter: %s.", f);
            }

            return new Parameter(pn, pt);
          }).toArray(Parameter[]::new);

          final var skipPc = vm.emit(Nop.instance);
          final var startPc = vm.emitPc();

          final var function = new Function(name, ps, resultType,
              (_function, _vm, _location, _parameters, _result) -> {
                _vm.pushCall(_function, _location, startPc, _result);

                for (var i = 0; i < _parameters.length; i++) {
                  vm.poke(i + 1, vm.peek(_parameters[i]));
                }
              });

          final var v = new Value<>(Function.type, function);

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
            vm.emit(new Poke(v, rResult));
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
        new Parameter[]{new Parameter("sequence", sequenceType)}, iteratorType,
        (function, vm, location, rParameters, rResult) -> {
          final var s = vm.peek(rParameters[0]);
          @SuppressWarnings("unchecked") final var st = (SequenceTrait<Value<?>>) s.type();
          vm.poke(rResult, new Value<>(iteratorType, st.iterator(s.data())));
        });

    bindFunction("length",
        new Parameter[]{new Parameter("collection", collectionType)}, integerType,
        (function, vm, location, rParameters, rResult) -> {
          final var c = vm.peek(rParameters[0]);
          final var ct = (CollectionTrait) c.type();
          vm.poke(rResult, new Value<>(integerType, ct.length(c.data())));
        });

    bindMacro("load", 1,
        (vm, namespace, location, arguments, rResult) -> {
          vm.evaluate(arguments[0], namespace, rResult);
          final var path = vm.peek(rResult).as(pathType);
          vm.poke(rResult, null);

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
          vm.emit(new Poke(new Value<>(Core.instance.dequeType, new ArrayDeque<>()), rResult));
          final var mapPc = vm.emit(Nop.instance);
          vm.emit(new CallRegister(rFunction, rValues, rCall, location));
          vm.emit(new AddLast(rCall, rResult));
          vm.emit(new Goto(mapPc));
          vm.emit(mapPc, new MapIterators(rFunction, rIterators, rValues, rResult, vm.emitPc(), location));
          vm.emit(new GetIterator(rResult, rResult, location));
        });

    bindFunction("parse-integer",
        new Parameter[]{new Parameter("input", stringType)}, pairType,
        (function, vm, location, rParameters, rResult) -> {
          final var input = vm.peek(rParameters[0]).as(stringType);
          final var match = Pattern.compile("^\\s*(\\d+).*").matcher(input);

          if (!match.find()) {
            throw new EvaluationError(location, "Invalid integer: %s", input);
          }

          vm.poke(rResult, new Value<>(pairType, new Pair(
              new Value<>(integerType, Integer.valueOf(match.group(1))),
              new Value<>(integerType, match.end(1)))));
        });

    bindFunction("path",
        new Parameter[]{new Parameter("path", stringType)}, pathType,
        (function, vm, location, rParameters, rResult) -> {
          vm.poke(rResult, new Value<>(pathType, Paths.get(vm.peek(rParameters[0]).as(stringType))));
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

    bindFunction("say",
        null, null,
        (function, vm, location, rParameters, rResult) -> {
          final var what = new StringBuilder();

          for (var i = 0; i < rParameters.length; i++) {
            if (i > 0) {
              what.append(' ');
            }

            what.append(vm.peek(rParameters[i]).say());
          }

          System.out.println(what);
          System.out.flush();
        });

    bindFunction("slice",
        new Parameter[]{
            new Parameter("input", indexedCollectionType),
            new Parameter("start", integerType)
        }, indexedCollectionType,
        (function, vm, location, rParameters, rResult) -> {
          final var i = vm.peek(rParameters[0]);
          final var it = (IndexedCollectionTrait) i.type();
          vm.poke(rResult, it.slice(i.data(), vm.peek(rParameters[1]).as(integerType)));
        });

    bindFunction("string",
        null, stringType,
        (function, vm, location, rParameters, rResult) -> {
          final var result = new StringBuilder();

          for (var i = 0; i < rParameters.length; i++) {
            result.append(vm.peek(rParameters[i]).say());
          }

          vm.poke(rResult, new Value<>(stringType, result.toString()));
        });

    bindFunction("reverse-string",
        new Parameter[]{new Parameter("input", stringType)}, stringType,
        (function, vm, location, rParameters, rResult) -> {
          final var result = new StringBuilder(vm.peek(rParameters[0]).as(stringType)).reverse().toString();
          vm.poke(rResult, new Value<>(stringType, result));
        });

    bindFunction("slurp",
        new Parameter[]{new Parameter("path", pathType)}, stringType,
        (function, vm, location, rParameters, rResult) -> {
          try {
            final var p = vm.loadPath().resolve(vm.peek(rParameters[0]).as(pathType));
            final String data = Files.readString(p);
            vm.poke(rResult, new Value<>(stringType, data));
          } catch (final IOException e) {
            throw new EvaluationError(location, "Failed reading file: %s", e);
          }
        });

    bindFunction("split",
        new Parameter[]{new Parameter("whole", stringType),
            new Parameter("separator", stringType)}, anyType,
        (function, vm, location, rParameters, rResult) -> {
          final var w = vm.peek(rParameters[0]).as(stringType);
          final var s = vm.peek(rParameters[1]).as(stringType);
          final String[] parts = w.split(Pattern.quote(s));
          final var result = new ArrayDeque<Value<?>>();

          for (final var p : parts) {
            result.add(new Value<>(stringType, p));
          }

          vm.poke(rResult, new Value<>(dequeType, result));
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
  }

  public final Type<Object> anyType = new Type<>("Any");
  public final BitType bitType = new BitType("Bit");
  public final Value<Boolean> T = new Value<>(bitType, true);
  public final Value<Boolean> F = new Value<>(bitType, false);
  public final CharacterType characterType = new CharacterType("Character");
  public final CollectionType collectionType = new CollectionType("Collection");
  public final ComparableType comparableType = new ComparableType("Comparable");
  public final DequeType dequeType = new DequeType("Deque");
  public final FloatType floatType = new FloatType("Float");
  public final IndexedCollectionType indexedCollectionType = new IndexedCollectionType("IndexedCollection");
  public final IntegerType integerType = new IntegerType("Integer");
  public final IteratorType iteratorType = new IteratorType("Iterator");
  public final NoneType noneType = new NoneType("None");
  public final Value<Object> NONE = new Value<>(noneType, null);
  public final PairType pairType = new PairType("Pair");
  public final Type<Path> pathType = new Type<>("Path");
  public final Type<Register> registerType = new Type<>("Register");
  public final SequenceType sequenceType = new SequenceType("Sequence");
  public final StringType stringType = new StringType("String");
}

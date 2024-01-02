package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.errors.EmitError;
import codr7.jalang.errors.EvaluationError;
import codr7.jalang.forms.DequeForm;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.operations.*;
import codr7.jalang.types.Compare;
import codr7.jalang.types.Pair;

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
      implements CollectionTrait, ComparableTrait, SequenceTrait<Value<Character>> {
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
  }

  public static final Core instance = new Core();

  public Core() {
    super("core", null);
    bindType(bitType);
    bindType(characterType);
    bindType(comparableType);
    bindType(dequeType);
    bindType(floatType);
    bindType(Function.type);
    bindType(integerType);
    bindType(iteratorType);
    bindType(Macro.type);
    bindType(Type.meta);
    bindType(pairType);
    bindType(pathType);
    bindType(registerType);
    bindType(stringType);

    bind("T", new Value<>(bitType, true));
    bind("F", new Value<>(bitType, false));

    bindFunction("=",
        new Parameter[]{new Parameter("left", anyType),
            new Parameter("right", anyType)}, bitType,
        (function, vm, location, in, out) -> {
          final var result = vm.peek(in[0]).equals(vm.peek(in[1]));
          vm.poke(out, new Value<>(bitType, result));
        });

    bindFunction("<",
        new Parameter[]{new Parameter("value1", comparableType)}, bitType,
        (function, vm, location, in, out) -> {
          var left = vm.peek(in[0]);
          var type = (ComparableTrait) left.type();
          var result = true;

          for (var i = 1; i < in.length; i++) {
            final var right = vm.peek(in[i]);

            if (right.type() != left.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", left.type(), right.type());
            }

            if (type.compare(left.data(), right.data()) != Compare.LessThan) {
              result = false;
              break;
            }

            left = right;
          }

          vm.poke(out, new Value<>(bitType, result));
        });

    bindFunction(">",
        new Parameter[]{new Parameter("value1", comparableType)}, bitType,
        (function, vm, location, in, out) -> {
          var left = vm.peek(in[0]);
          var type = (ComparableTrait) left.type();
          var result = true;

          for (var i = 1; i < in.length; i++) {
            final var right = vm.peek(in[i]);

            if (right.type() != left.type()) {
              throw new EvaluationError(location, "Type mismatch: %s/%s.", left.type(), right.type());
            }

            if (type.compare(left.data(), right.data()) != Compare.GreaterThan) {
              result = false;
              break;
            }

            left = right;
          }

          vm.poke(out, new Value<>(bitType, result));
        });

    bindFunction("+", null, integerType,
        (function, vm, location, in, out) -> {
          int result = 0;

          for (var i = 0; i < in.length; i++) {
            result += vm.peek(in[i]).as(integerType);
          }

          vm.poke(out, new Value<>(integerType, result));
        });

    bindFunction("-", null, integerType,
        (function, vm, location, in, out) -> {
          int result = vm.peek(in[0]).as(integerType);

          if (in.length == 1) {
            result = -result;
          } else {
            for (var i = 1; i < in.length; i++) {
              result -= vm.peek(in[i]).as(integerType);
            }
          }

          vm.poke(out, new Value<>(integerType, result));
        });

    bindMacro("+1", 1,
        (vm, namespace, location, arguments, register) -> {
          final var a = arguments[0];
          int valueRegister;

          if (a instanceof Identifier) {
            final var v = namespace.find(((Identifier) a).name());

            if (v.type() != registerType) {
              throw new EmitError(location, "Invalid target: %s", v.toString());
            }

            final var r = (Register) v.data();

            if (r.type() != null && r.type() != integerType) {
              throw new EmitError(location, "Invalid target: %s", r.type());
            }

            valueRegister = r.index();
          } else if (a instanceof Literal) {
            valueRegister = vm.allocateRegister();
            vm.poke(valueRegister, ((Literal) a).value());
          } else {
            throw new EmitError(location, "Invalid target: %s", a.toString());
          }

          vm.emit(new Increment(valueRegister, register));
        });

    bindMacro("-1", 1, (vm, namespace, location, arguments, register) -> {
      final var a = arguments[0];
      int valueRegister;

      if (a instanceof Identifier) {
        final var v = namespace.find(((Identifier) a).name());

        if (v.type() != registerType) {
          throw new EmitError(location, "Invalid target: %s", v.toString());
        }

        final var r = (Register) v.data();

        if (r.type() != null && r.type() != integerType) {
          throw new EmitError(location, "Invalid target: %s", r.type());
        }

        valueRegister = r.index();
      } else if (a instanceof Literal) {
        valueRegister = vm.allocateRegister();
        vm.poke(valueRegister, ((Literal) a).value());
      } else {
        throw new EmitError(location, "Invalid target: %s", a.toString());
      }

      vm.emit(new Decrement(valueRegister, register));
    });

    bindMacro("benchmark", 1,
        (vm, namespace, location, arguments, register) -> {
          final var repetitions = vm.allocateRegister();
          arguments[0].emit(vm, namespace, repetitions);
          vm.emit(new Benchmark(repetitions, register));

          for (int i = 1; i < arguments.length; i++) {
            arguments[i].emit(vm, namespace, repetitions);
          }

          vm.emit(Stop.instance);
        });

    bindMacro("check", 2, (vm, namespace, location, arguments, register) -> {
      final var expectedRegister = vm.allocateRegister();
      arguments[0].emit(vm, namespace, expectedRegister);
      final var actualRegister = vm.allocateRegister();
      vm.emit(new Check(expectedRegister, actualRegister, location));
      final var bodyNamespace = new Namespace(namespace);

      for (var i = 1; i < arguments.length; i++) {
        arguments[i].emit(vm, bodyNamespace, actualRegister);
      }

      vm.emit(Stop.instance);
    });

    bindFunction("deque",
        new Parameter[]{new Parameter("input", sequenceType)}, dequeType,
        (function, vm, location, in, out) -> {
          final var input = vm.peek(in[0]);

          @SuppressWarnings("unchecked") final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input.data());
          final var result = new ArrayDeque<Value<?>>();

          while (iterator.hasNext()) {
            result.add(iterator.next());
          }

          vm.poke(out, new Value<>(dequeType, result));
        });

    bindMacro("function", 1,
        (vm, namespace, location, arguments, register) -> {
          final var as = new ArrayDeque<Form>();
          Collections.addAll(as, arguments);
          var name = "";

          if (arguments[0] instanceof Identifier) {
            name = ((Identifier) as.removeFirst()).name();
          }

          var psForm = as.removeFirst();
          Type<?> resultType = null;

          if (psForm instanceof Pair.Form) {
            var p = (Pair.Form) psForm;
            var tnf = p.right();
            var tv = namespace.find(((Identifier) tnf).name());

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

            if (f instanceof Pair.Form) {
              final var pf = (Pair.Form) f;
              if (!((pf.left() instanceof Identifier) && (pf.right() instanceof Identifier))) {
                throw new EmitError(f.location(), "Invalid parameter: %s.", pf);
              }

              final var tf = ((Identifier) pf.left());
              pn = tf.name();
              final var tv = namespace.find(((Identifier) pf.right()).name());

              if (tv == null) {
                throw new EmitError(tf.location(), "Type not found: %s.", tf);
              }

              pt = tv.as(Type.meta);
            } else if (f instanceof Identifier) {
              pn = ((Identifier) f).name();
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
            f.emit(vm, bodyNamespace, register);
          }

          vm.emit(new Return(register));
          vm.emit(skipPc, new Goto(vm.emitPc()));

          if (name.isEmpty()) {
            vm.emit(new Poke(register, v));
          }
        });

    bindFunction("head",
        new Parameter[]{new Parameter("pair", pairType)}, anyType,
        (function, vm, location, in, out) -> {
          vm.poke(out, vm.peek(in[0]).as(pairType).left());
        });

    bindMacro("if", 2,
        (vm, namespace, location, arguments, register) -> {
          arguments[0].emit(vm, namespace, register);
          final var ifPc = vm.emit(Nop.instance);
          arguments[1].emit(vm, namespace, register);
          final var skipPc = (arguments.length > 2) ? vm.emit(Nop.instance) : -1;
          vm.emit(ifPc, new If(register, vm.emitPc()));

          if (skipPc != -1) {
            arguments[2].emit(vm, namespace, register);
            vm.emit(skipPc, new Goto(vm.emitPc()));
          }
        });

    bindFunction("iterator",
        new Parameter[]{new Parameter("sequence", sequenceType)}, iteratorType,
        (function, vm, location, in, out) -> {
          final var s = vm.peek(in[0]);
          @SuppressWarnings("unchecked") final var st = (SequenceTrait<Value<?>>) s.type();
          vm.poke(out, new Value<>(iteratorType, st.iterator(s.data())));
        });

    bindFunction("length",
        new Parameter[]{new Parameter("collection", collectionType)}, integerType,
        (function, vm, location, in, out) -> {
          final var c = vm.peek(in[0]);
          final var ct = (CollectionTrait) c.type();
          vm.poke(out, new Value<>(integerType, ct.length(c.data())));
        });

    bindMacro("load", 1,
        (vm, namespace, location, arguments, register) -> {
          vm.evaluate(arguments[0], register);
          final var path = vm.peek(register).as(pathType);
          vm.poke(register, null);

          try {
            vm.load(path, register);
          } catch (final IOException e) {
            throw new EmitError(location, e.toString());
          }
          });

    bindFunction("map",
        new Parameter[]{new Parameter("function", Function.type),
            new Parameter("input1", sequenceType)}, anyType,
        (function, vm, location, in, out) -> {
          final var inputs = new ArrayList<Iterator<Value<?>>>();
          final var inputRegisters = new int[in.length - 1];

          for (var i = 1; i < in.length; i++) {
            final var v = vm.peek(in[i]);
            inputs.add(((SequenceTrait<Value<?>>) v.type()).iterator(v.data()));
            inputRegisters[i - 1] = in[i];
          }

          final var f = vm.peek(in[0]).as(Function.type);

          final var result = new Iterator<Value<?>>() {
            @Override
            public boolean hasNext() {
              for (var i = 0; i < inputs.size(); i++) {
                final var in = inputs.get(i);

                if (!in.hasNext()) {
                  return false;
                }

                vm.poke(inputRegisters[i], in.next());
              }

              return true;
            }

            @Override
            public Value<?> next() {
              f.call(vm, location, inputRegisters, out);
              return vm.peek(out);
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };

          vm.poke(out, new Value<>(iteratorType, result));
        });

    bindFunction("parse-integer",
        new Parameter[]{new Parameter("input", stringType)}, pairType,
        (function, vm, location, in, out) -> {
          final var input = vm.peek(in[0]).as(stringType);
          final var match = Pattern.compile("^\\s*(\\d+).*").matcher(input);

          if (!match.find()) {
            throw new EvaluationError(location, "Invalid integer: %s", input);
          }

          vm.poke(out, new Value<>(pairType, new Pair(
              new Value<>(integerType, Integer.valueOf(match.group(1))),
              new Value<>(integerType, match.end(1)))));
        });

    bindFunction("path",
        new Parameter[]{new Parameter("path", stringType)}, pathType,
        (function, vm, location, in, out) -> {
          vm.poke(out, new Value<>(pathType, Paths.get(vm.peek(in[0]).as(stringType))));
        });

    bindFunction("reduce",
        new Parameter[]{new Parameter("function", Function.type),
            new Parameter("input", sequenceType),
            new Parameter("seed", anyType)}, anyType,
        (function, vm, location, in, out) -> {
          final var f = vm.peek(in[0]).as(Function.type);
          final var input = vm.peek(in[1]);
          final var iterator = ((SequenceTrait<Value<?>>) input.type()).iterator(input.data());
          var result = vm.peek(in[2]);
          final var inputParameters = new int[2];
          inputParameters[0] = in[0];
          inputParameters[1] = in[1];

          while (iterator.hasNext()) {
            vm.poke(inputParameters[0], iterator.next());
            vm.poke(inputParameters[1], result);
            f.call(vm, location, inputParameters, out);
            result = vm.peek(out);
          }
        });

    bindFunction("say",
        null, null,
        (function, vm, location, in, out) -> {
          final var what = new StringBuilder();

          for (var i = 0; i < in.length; i++) {
            if (i > 0) {
              what.append(' ');
            }

            what.append(vm.peek(in[i]).say());
          }

          System.out.println(what);
          System.out.flush();
        });

    bindFunction("string",
        null, stringType,
        (function, vm, location, in, out) -> {
          final var result = new StringBuilder();

          for (var i = 0; i < in.length; i++) {
            result.append(vm.peek(in[i]).say());
          }

          vm.poke(out, new Value<>(stringType, result.toString()));
        });

    bindFunction("reverse-string",
        new Parameter[]{new Parameter("input", stringType)}, stringType,
        (function, vm, location, in, out) -> {
          final var result = new StringBuilder(vm.peek(in[0]).as(stringType)).reverse().toString();
          vm.poke(out, new Value<>(stringType, result));
        });

    bindFunction("slurp",
        new Parameter[]{new Parameter("path", pathType)}, stringType,
        (function, vm, location, in, out) -> {
          try {
            final var p = vm.loadPath().resolve(vm.peek(in[0]).as(pathType));
            final String data = Files.readString(p);
            vm.poke(out, new Value<>(stringType, data));
          } catch (final IOException e) {
            throw new EvaluationError(location, "Failed reading file: %s", e);
          }
        });

    bindFunction("split",
        new Parameter[]{new Parameter("whole", stringType),
            new Parameter("separator", stringType)}, anyType,
        (function, vm, location, in, out) -> {
          final var w = vm.peek(in[0]).as(stringType);
          final var s = vm.peek(in[1]).as(stringType);
          final String[] parts = w.split(Pattern.quote(s));
          final var result = new ArrayDeque<Value<?>>();

          for (final var p : parts) {
            result.add(new Value<>(stringType, p));
          }

          vm.poke(out, new Value<>(dequeType, result));
        });

    bindFunction("tail",
        new Parameter[]{new Parameter("pair", pairType)}, anyType,
        (function, vm, location, in, out) -> {
          vm.poke(out, vm.peek(in[0]).as(pairType).right());
        });

    bindMacro("trace", 0,
        (vm, namespace, location, in, out) -> {
          vm.toggleTracing();
        });
  }

  public final Type<Object> anyType = new Type<>("Any");
  public final BitType bitType = new BitType("Bit");
  public final CharacterType characterType = new CharacterType("Character");
  public final CollectionType collectionType = new CollectionType("Collection");
  public final ComparableType comparableType = new ComparableType("Comparable");
  public final DequeType dequeType = new DequeType("Deque");
  public final FloatType floatType = new FloatType("Float");
  public final IntegerType integerType = new IntegerType("Integer");
  public final IteratorType iteratorType = new IteratorType("Iterator");
  public final PairType pairType = new PairType("Pair");
  public final Type<Path> pathType = new Type<>("Path");
  public final Type<Register> registerType = new Type<>("Register");
  public final SequenceType sequenceType = new SequenceType("Sequence");
  public final StringType stringType = new StringType("String");
}

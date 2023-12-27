package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.operations.Check;
import codr7.jalang.operations.Decrement;
import codr7.jalang.operations.Increment;
import codr7.jalang.operations.Stop;
import codr7.jalang.types.Pair;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.Iterator;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class Core extends Library {
  public interface CollectionTrait {
    int length(final Object value);
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

  public static class CharacterType extends Type<Character> {
    public CharacterType(final String name) {
      super(name);
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

      for (final var v: value) {
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

    public Iterator<Value<?>> iterator(final Object value) {
      return ((Deque<Value<?>>)value).iterator();
    }

    public int length(final Object value) {
      return ((Deque<Value<?>>)value).size();
    }
  }

  public static class IntegerType extends Type<Integer> implements SequenceTrait<Value<Integer>> {
    public IntegerType(final String name) {
      super(name);
    }

    public boolean isTrue(final Integer value) {
      return value != 0;
    }

    public Iterator<Value<Integer>> iterator(final Object value) {
      return Stream
          .iterate(0, x -> x + 1)
          .limit((Integer)value)
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
      implements CollectionTrait, SequenceTrait<Value<Character>> {
    public StringType(final String name) {
      super(name);
    }
    public String dump(final String value) {
      return String.format("\"%s\"", value);
    }
    public boolean isTrue(String value) {
      return !value.isEmpty();
    }

    public Iterator<Value<Character>> iterator(final Object value) {
      return ((String)value).codePoints()
          .mapToObj((c) -> new Value<Character>(Core.instance.characterType, (char)c))
          .iterator();
    }

    public int length(final Object value) {
      return ((String)value).length();
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
    bindType(dequeType);
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
        (vm, location, arity, register) -> {
      final var result = vm.peek(1).equals(vm.peek(2));
      vm.poke(register, new Value<>(bitType, result));
    });

    bindFunction("+", null, integerType, (vm, location, arity, register) -> {
      int result = 0;

      for (var i = 1; i <= arity; i++) {
        result += vm.peek(i).as(integerType);
      }

      vm.poke(register, new Value<>(integerType, result));
    });

    bindFunction("-", null, integerType, (vm, location, arity, register) -> {
      if (arity > 0) {
        int result = vm.peek(1).as(integerType);

        if (arity == 1) {
          result = -result;
        } else {
          for (var i = 2; i <= arity; i++) {
            result -= vm.peek(i).as(integerType);
          }
        }

        vm.poke(register, new Value<>(integerType, result));
      }
    });

    bindMacro("+1", 1, (vm, namespace, location, arguments, register) -> {
      final var a = arguments[0];
      int valueRegister = -1;

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
        vm.poke(valueRegister, ((Literal)a).value());
      } else {
        throw new EmitError(location, "Invalid target: %s", a.toString());
      }

      vm.emit(new Increment(valueRegister, register));
    });

    bindMacro("-1", 1, (vm, namespace, location, arguments, register) -> {
      final var a = arguments[0];
      int valueRegister = -1;

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
        vm.poke(valueRegister, ((Literal)a).value());
      } else {
        throw new EmitError(location, "Invalid target: %s", a.toString());
      }

      vm.emit(new Decrement(valueRegister, register));
    });

    bindMacro("check", 2, (vm, namespace, location, arguments, register) -> {
      final var expectedRegister = vm.allocateRegister();
      arguments[0].emit(vm, namespace, expectedRegister);
      final var actualRegister = vm.allocateRegister();
      vm.emit(new Check(expectedRegister, actualRegister, location));

      for (var i = 1; i < arguments.length; i++) {
        arguments[i].emit(vm, namespace, actualRegister);
      }

      vm.emit(Stop.instance);
    });

    bindFunction("deque",
        new Parameter[]{new Parameter("input", sequenceType)}, dequeType,
        (vm, location, arity, register) -> {
      final var input = vm.peek(1);
      final var iterator = ((SequenceTrait<Value<?>>)input.type()).iterator(input.data());
      final var result = new ArrayDeque<Value<?>>();

      while (iterator.hasNext()) {
        result.add(iterator.next());
      }

      vm.poke(register, new Value<>(dequeType, result));
    });

    bindFunction("head",
        new Parameter[]{new Parameter("pair", pairType)}, anyType,
        (vm, location, arity, register) -> {
      vm.poke(register, vm.peek(1).as(pairType).left());
    });

    bindFunction("iterator",
        new Parameter[]{new Parameter("sequence", sequenceType)}, iteratorType,
        (vm, location, arity, register) -> {
          final var s = vm.peek(1);
          final var st = (SequenceTrait<Value<?>>)s.type();
          vm.poke(register, new Value<>(iteratorType, st.iterator(s.data())));
        });

    bindFunction("length",
        new Parameter[]{new Parameter("collection", collectionType)}, integerType,
        (vm, location, arity, register) -> {
        final var c = vm.peek(1);
        final var ct = (CollectionTrait)c.type();
        vm.poke(register, new Value<>(integerType, ct.length(c.data())));
    });

    bindFunction("map",
        new Parameter[]{new Parameter("function", Function.type),
            new Parameter("input1", sequenceType)}, anyType,
        (vm, location, arity, register) -> {
          final var inputs = new ArrayList<Iterator<Value<?>>>();

          for (var i = 2; i <= arity; i++) {
            final var v = vm.peek(i);
            inputs.add(((SequenceTrait<Value<?>>)v.type()).iterator(v.data()));
          }

          final var f = vm.peek(1).as(Function.type);

          final var result = new Iterator<Value<?>>() {
            @Override
            public boolean hasNext() {
              for (var i = 0; i < inputs.size(); i++) {
                final var in = inputs.get(i);

                if (!in.hasNext()) {
                  return false;
                }

                vm.poke(i + 1, in.next());
              }

              return true;
            }

            @Override
            public Value<?> next() {
              f.call(vm, location, inputs.size(), register);
              return vm.peek(register);
            }

            @Override
            public void remove() {
              throw new UnsupportedOperationException();
            }
          };

          vm.poke(register, new Value<>(iteratorType, result));
        });

    bindFunction("parse-integer",
        new Parameter[]{new Parameter("input", stringType)}, pairType,
        (vm, location, arity, register) -> {
      final var input = vm.peek(1).as(stringType);
      final var match = Pattern.compile("^\\s*(\\d+).*").matcher(input);

      if (!match.find()) {
        throw new EvaluationError(location, "Invalid integer: %s", input);
      }

      vm.poke(register, new Value<>(pairType, new Pair(
          new Value<>(integerType, Integer.valueOf(match.group(1))),
          new Value<>(integerType, match.end(1)))));
    });

    bindFunction("path",
        new Parameter[]{new Parameter("path", stringType)}, pathType,
        (vm, location, arity, register) -> {
      vm.poke(register, new Value<>(pathType, Paths.get(vm.peek(1).as(stringType))));
    });

    bindFunction("say",
        null, null,
        (vm, location, arity, register) -> {
      final var what = new StringBuilder();

      for (var i = 1; i <= arity; i++) {
        if (i > 1) {
          what.append(' ');
        }

        what.append(vm.peek(i).say());
      }

      System.out.println(what.toString());
    });

    bindFunction("string",
        null, stringType,
        (vm, location, arity, register) -> {
          final var result = new StringBuilder();

          for (var i = 1; i <= arity; i++) {
            result.append(vm.peek(i).say());
          }

          vm.poke(register, new Value<>(stringType, result.toString()));
    });

    bindFunction("reverse-string",
        new Parameter[]{new Parameter("input", stringType)}, stringType,
        (vm, location, arity, register) -> {
      final var result = new StringBuilder(vm.peek(1).as(stringType)).reverse().toString();
      vm.poke(register, new Value<>(stringType, result));
    });

    bindFunction("slurp",
        new Parameter[]{new Parameter("path", pathType)}, stringType,
        (vm, location, arity, register) -> {
      try {
        final var p = vm.loadPath().resolve(vm.peek(1).as(pathType));
        final String data = Files.readString(p);
        vm.poke(register, new Value<>(stringType, data));
      } catch (final IOException e) {
        throw new EvaluationError(location, "Failed reading file: %s", e);
      }
    });

    bindFunction("split",
        new Parameter[]{new Parameter("whole", stringType),
            new Parameter("separator", stringType)}, anyType,
        (vm, location, arity, register) -> {
          final var w = vm.peek(1).as(stringType);
          final var s = vm.peek(2).as(stringType);
          final String[] parts = w.split(Pattern.quote(s));
          final var result = new ArrayDeque<Value<?>>();

          for (final var p: parts) {
            result.add(new Value<>(stringType, p));
          }

          vm.poke(register, new Value<>(dequeType, result));
        });

        bindFunction("tail",
        new Parameter[]{new Parameter("pair", pairType)}, anyType,
        (vm, location, arity, register) -> {
          vm.poke(register, vm.peek(1).as(pairType).right());
        });

    bindMacro("trace", 1, (vm, namespace, location, arguments, register) -> {
      vm.toggleTracing();
    });
  }

  public final Type<Object> anyType = new Type<>("Any");
  public final BitType bitType = new BitType("Bit");
  public final CharacterType characterType = new CharacterType("Character");
  public final CollectionType collectionType = new CollectionType("Collection");
  public final DequeType dequeType = new DequeType("Deque");
  public final IntegerType integerType = new IntegerType("Integer");
  public final IteratorType iteratorType = new IteratorType("Iterator");
  public final PairType pairType = new PairType("Pair");
  public final Type<Path> pathType = new Type<>("Path");
  public final Type<Register> registerType = new Type<>("Register");
  public final SequenceType sequenceType = new SequenceType("Sequence");
  public final StringType stringType = new StringType("String");
}

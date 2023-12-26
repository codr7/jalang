package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.operations.Decrement;
import codr7.jalang.operations.Increment;
import codr7.jalang.types.Pair;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Deque;

public class Core extends Library {
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

  public static class DequeType extends Type<Deque<Value<?>>> {
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

    public boolean isTrue(final Deque<Value<?>> value) {
      return !value.isEmpty();
    }
  }

  public static class IntType extends Type<Integer> {
    public IntType(final String name) {
      super(name);
    }

    public boolean isTrue(final Integer value) {
      return value != 0;
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

  public static class StringType extends Type<String> {
    public StringType(final String name) {
      super(name);
    }

    public String dump(final String value) {
      return String.format("\"%s\"", value);
    }

    public boolean isTrue(String value) {
      return !value.isEmpty();
    }
  }

  public static final Core instance = new Core();
  public Core() {
    super("core", null);
    bindType(bitType);
    bindType(dequeType);
    bindType(Function.type);
    bindType(intType);
    bindType(Macro.type);
    bindType(Type.meta);
    bindType(pairType);
    bindType(registerType);
    bindType(stringType);

    bind("T", new Value<>(bitType, true));
    bind("F", new Value<>(bitType, false));

    bindFunction("+", null, intType, (vm, location, arity, register) -> {
      int result = 0;

      for (int i = 1; i <= arity; i++) {
        result += vm.peek(i).as(intType);
      }

      vm.poke(register, new Value<>(intType, result));
    });

    bindFunction("-", null, intType, (vm, location, arity, register) -> {
      if (arity > 0) {
        int result = vm.peek(1).as(intType);

        if (arity == 1) {
          result = -result;
        } else {
          for (int i = 2; i <= arity; i++) {
            result -= vm.peek(i).as(intType);
          }
        }

        vm.poke(register, new Value<>(intType, result));
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

        if (r.type() != null && r.type() != intType) {
          throw new EmitError(location, "Invalid target: %s", r.type());
        }

        valueRegister = r.index();
      } else if (a instanceof Literal) {
        valueRegister = 1;
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

        if (r.type() != null && r.type() != intType) {
          throw new EmitError(location, "Invalid target: %s", r.type());
        }

        valueRegister = r.index();
      } else if (a instanceof Literal) {
        valueRegister = 1;
        vm.poke(valueRegister, ((Literal)a).value());
      } else {
        throw new EmitError(location, "Invalid target: %s", a.toString());
      }

      vm.emit(new Decrement(valueRegister, register));
    });

    bindFunction("slurp",
        new Parameter[]{new Parameter("path", stringType)}, stringType,
        (vm, location, arity, register) -> {
      try {
        String data = Files.readString(Paths.get(vm.peek(1).as(stringType)));
        vm.poke(register, new Value<>(stringType, data));
      } catch (final IOException e) {
        throw new EvaluationError(location, "Failed reading file: %s", e);
      }
    });

    bindMacro("trace", 1, (vm, namespace, location, arguments, register) -> {
      vm.toggleTracing();
    });
  }

  public final BitType bitType = new BitType("Bit");
  public final DequeType dequeType = new DequeType("Deque");
  public final IntType intType = new IntType("Int");
  public final PairType pairType = new PairType("Pair");
  public final Type<Register> registerType = new Type<>("Register");
  public final StringType stringType = new StringType("String");
}

package codr7.jalang.libraries;

import codr7.jalang.*;
import codr7.jalang.forms.Identifier;
import codr7.jalang.forms.Literal;
import codr7.jalang.operations.Decrement;
import codr7.jalang.operations.Increment;
import codr7.jalang.types.Pair;

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

    public boolean isTrue(final Pair value) {
      return value.left().isTrue();
    }
  }

  public static class StringType extends Type<String> {
    public StringType(final String name) {
      super(name);
    }

    public boolean isTrue(String value) {
      return !value.isEmpty();
    }
  }

  public static final Core instance = new Core();
  public Core() {
    super("core", null);
    bindType(bitType);
    bindType(Function.type);
    bindType(intType);
    bindType(Macro.type);
    bindType(Type.meta);
    bindType(pairType);
    bindType(registerType);
    bindType(stringType);

    bind("T", new Value<>(bitType, true));
    bind("F", new Value<>(bitType, false));

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

    bindFunction("+", -1, (vm, location, arity, register) -> {
      int result = 0;

      for (int i = 1; i <= arity; i++) {
        result += (int)vm.peek(i).data();
      }

      vm.poke(register, new Value<>(intType, result));
    });

    bindFunction("-", -1, (vm, location, arity, register) -> {
      if (arity > 0) {
        int result = (int) vm.peek(1).data();

        if (arity == 1) {
          result = -result;
        } else {
          for (int i = 2; i <= arity; i++) {
            result -= (int) vm.peek(i).data();
          }
        }

        vm.poke(register, new Value<>(intType, result));
      }
    });
  }

  public final BitType bitType = new BitType("Bit");
  public final IntType intType = new IntType("Int");
  public final PairType pairType = new PairType("Pair");
  public final Type<Register> registerType = new Type<>("Register");
  public final StringType stringType = new StringType("String");
}

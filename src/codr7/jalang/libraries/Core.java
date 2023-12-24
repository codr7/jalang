package codr7.jalang.libraries;

import codr7.jalang.Library;
import codr7.jalang.types.Pair;
import codr7.jalang.Type;
import codr7.jalang.Value;

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
    bindType(intType);
    bindType(Type.meta);
    bindType(pairType);
    bindType(stringType);

    bind("T", new Value<>(bitType, true));
    bind("F", new Value<>(bitType, false));

    bindFunction("+", -1, (vm, location, arity, register) -> {
      int result = 0;

      for (int i = 1; i <= arity; i++) {
        result += (int)vm.peek(i).data();
      }

      vm.poke(register, new Value<>(intType, result));
    });
  }

  public final BitType bitType = new BitType("Bit");
  public final IntType intType = new IntType("Int");
  public final PairType pairType = new PairType("Pair");
  public final StringType stringType = new StringType("String");
}

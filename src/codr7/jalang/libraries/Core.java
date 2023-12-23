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

    public boolean isTrue(Value<Boolean> value) {
      return value.data();
    }
  }
  public static class IntType extends Type<Integer> {
    public IntType(final String name) {
      super(name);
    }

    public boolean isTrue(Value<Integer> value) {
      return value.data() != 0;
    }
  }

  public static class PairType extends Type<Pair> {
    public PairType(final String name) {
      super(name);
    }

    public boolean isTrue(Value<Pair> value) {
      return value.data().left().isTrue();
    }
  }

  public static class StrType extends Type<String> {
    public StrType(final String name) {
      super(name);
    }

    public boolean isTrue(Value<String> value) {
      return !value.data().isEmpty();
    }
  }

  public static final Core instance = new Core();
  public Core() {
    super("core", null);
  }

  public final BitType bitType = new BitType("Bit");
  public final IntType intType = new IntType("Int");
  public final PairType pairType = new PairType("Pair");
  public final StrType strType = new StrType("Str");
}

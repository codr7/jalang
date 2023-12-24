package codr7.jalang;

public class Function {
  public static final Type<Function> type = new Type<>("Function");
  public interface Body {
    void call(final Vm vm, final Location location, final int arity, final int register);
  }

  public Function(final String name, final int arity, final Body body) {
    this.name = name;
    this.arity = arity;
    this.body = body;
  }

  public final int arity() {
    return arity;
  }

  public final void call(final Vm vm, final Location location, final int arity, final int register) {
    body.call(vm, location, arity, register);
  }

  public final String toString() {
    return String.format("(Function %s/%d)", name, arity);
  }

  private final String name;
  private final int arity;
  private final Body body;
}

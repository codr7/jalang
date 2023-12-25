package codr7.jalang;

public record Function(String name, int arity, Body body) {
  public static final Type<Function> type = new Type<>("Function");
  public interface Body {
    void call(final Vm vm, final Location location, final int arity, final int register);
  }

  public final void call(final Vm vm, final Location location, final int arity, final int register) {
    body.call(vm, location, arity, register);
  }

  public final String toString() {
    return String.format("(Function %s/%d)", name, arity);
  }
}

package codr7.jalang;

public record Macro(String name, int arity, Body body) {
  public static final Type<Macro> type = new Type<>("Macro");

  public interface Body {
    void call(final Vm vm,
              final Namespace namespace,
              final Location location,
              final Form[] arguments,
              final int register);
  }

  public void call(final Vm vm,
                   final Namespace namespace,
                   final Location location,
                   final Form[] arguments,
                   final int register) {
    body.call(vm, namespace, location, arguments, register);
  }

  public String toString() {
    return String.format("(Macro %s/%d)", name, arity);
  }
}

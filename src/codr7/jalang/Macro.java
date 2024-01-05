package codr7.jalang;

public record Macro(String name, int arity, Body body) {

  public interface Body {
    void call(final Vm vm,
              final Namespace namespace,
              final Location location,
              final Form[] arguments,
              final int rResult);
  }

  public void call(final Vm vm,
                   final Namespace namespace,
                   final Location location,
                   final Form[] arguments,
                   final int rResult) {
    body.call(vm, namespace, location, arguments, rResult);
  }

  public String toString() {
    return String.format("(Macro %s/%d)", name, arity);
  }
}

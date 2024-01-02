package codr7.jalang;

public record Function(String name, Parameter[] parameters, Type<?> resultType, Body body) {
  public static final Type<Function> type = new Type<>("Function");

  public interface Body {
    void call(final Function function,
              final Vm vm,
              final Location location,
              final int[] parameters,
              final int result);
  }

  public int arity() {
    return (parameters == null) ? -1 : parameters.length;
  }

  public void call(final Vm vm, final Location location, final int[] parameters, final int result) {
    body.call(this, vm, location, parameters, result);
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append("(Function ").append(name).append('[');

    if (parameters != null) {
      for (final var p : parameters) {
        result.append(' ');
        result.append(p);
      }
    }

    result.append(']');

    if (resultType != null) {
      result.append(':').append(resultType);
    }

    result.append(')');
    return result.toString();
  }
}

package codr7.jalang;

public record Function(String name, Parameter[] parameters, int arity, Type<?> resultType, Body body) {
  public static final Type<Function> type = new Type<>("Function");

  public interface Body {
    void call(final Function function,
              final Vm vm,
              final Location location,
              final int[] rParameters,
              final int rResult);
  }

  public int arity() {
    return arity;
  }

  public void call(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
    body.call(this, vm, location, rParameters, rResult);
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append("(Function ").append(name).append('[');

    if (parameters != null) {
      for (var i = 0; i < parameters.length; i++) {
        if (i > 0) {
          result.append(' ');
        }

        result.append(parameters[i]);
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

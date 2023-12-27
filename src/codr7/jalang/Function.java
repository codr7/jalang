package codr7.jalang;

public record Function(String name, Parameter[] parameters, Type<?> resultType, Body body) {
  public static final Type<Function> type = new Type<>("Function");

  public interface Body {
    void call(final Vm vm, final Location location, final int arity, final int register);
  }

  public int arity() {
    return (parameters == null) ? -1 : parameters.length;
  }

  public void call(final Vm vm, final Location location, final int arity, final int register) {
    body.call(vm, location, arity, register);
  }

  public String toString() {
    final var result = new StringBuilder();
    result.append("(Function ").append(name);

    if (parameters != null) {
      for (var i = 0; i < parameters.length; i++) {
        if (i > 0) {
          result.append(' ');
        }

        result.append(parameters[i]);
      }
    }

    result.append(')');

    if (resultType != null) {
      result.append(':').append(resultType);
    }

    return result.toString();
  }
}

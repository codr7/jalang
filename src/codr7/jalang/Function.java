package codr7.jalang;

public record Function(String name, Parameter[] parameters, Body body) {
    public int arity() {
        return parameters.length;
    }

    public void call(final Location location, final Namespace namespace, final int[] rParameters, final int rResult) {
        body.call(this, location, namespace, rParameters, rResult);
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

        result.append("])");
        return result.toString();
    }

    public interface Body {
        void call(final Function function,
                  final Location location,
                  final Namespace namespace,
                  final int[] rParameters,
                  final int rResult);
    }

    public record Parameter(String name, int rValue) {
        public String toString() {
            return name;
        }
    }
}

package codr7.jalang.errors;

import codr7.jalang.Location;

public class EvaluationError extends RuntimeException {
  public EvaluationError(final Location location, final String format, final Object... arguments) {
    super(String.format("EvaluationError in %s: %s", location.toString(), String.format(format, arguments)));
    this.location = location;
  }

  public final Location location() {
    return location;
  }

  private final Location location;
}

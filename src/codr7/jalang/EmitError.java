package codr7.jalang;

public class EmitError extends RuntimeException {
  public EmitError(final Location location, final String format, final Object... arguments) {
    super(String.format("EmitError in %s: %s", location.toString(), String.format(format, arguments)));
    this.location = location;
  }

  public final Location location() {
    return location;
  }

  private final Location location;
}

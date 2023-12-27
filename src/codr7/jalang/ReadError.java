package codr7.jalang;

public class ReadError extends RuntimeException {
  public ReadError(final Location location, final String format, final Object... arguments) {
    super(String.format("ReadError in %s: %s", location.toString(), String.format(format, arguments)));
    this.location = location;
  }

  public final Location location() {
    return location;
  }

  private final Location location;
}


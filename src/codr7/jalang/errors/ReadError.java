package codr7.jalang.errors;

import codr7.jalang.Location;

public class ReadError extends RuntimeException {
  private final Location location;

  public ReadError(final Location location, final String format, final Object... arguments) {
    super(String.format("ReadError in %s: %s", location.toString(), String.format(format, arguments)));
    this.location = location;
  }

  public final Location location() {
    return location;
  }
}


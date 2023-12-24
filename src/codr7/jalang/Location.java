package codr7.jalang;

public final class Location {
  public Location(final String source) {
    this(source, 1, 1);
  }

  public Location clone() {
    return new Location(source, line, column);
  }

  public final void update(final char c) {
    if (c == '\n') {
      line++;
      column = 1;
    } else {
      column++;
    }
  }

  public final String toString() {
    return String.format("%s@%d:%d", source, line, column);
  }

  private Location(final String source, final int line, final int column) {
    this.source = source;
    this.line = line;
    this.column = column;
  }

  public final String source;
  private int line, column;
}

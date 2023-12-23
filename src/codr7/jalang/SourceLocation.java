package codr7.jalang;

public final class SourceLocation {
  private final String source;
  private int line, column;

  public SourceLocation(final String source) {
    this.source = source;
    this.line = 1;
    this.column = 1;
  }

  public void update(final char c) {
    if (c == '\n') {
      line++;
      column = 1;
    } else {
      column++;
    }
  }

  public String toString() {
    return String.format("%s@%d:%d", source, line, column);
  }
}

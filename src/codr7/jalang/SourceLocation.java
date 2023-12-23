package codr7.jalang;

public final class SourceLocation {
  public SourceLocation(final String source) {
    this.source = source;
    this.line = 1;
    this.column = 1;
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

  public final String source;
  private int line, column;
}

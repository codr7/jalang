package codr7.jalang;

public record Parameter(String name, Type<?> type) {
  public String toString() {
    final var result = new StringBuilder();
    result.append(name);

    if (type != null) {
      result.append(':').append(type);
    }

    return result.toString();
  }
}

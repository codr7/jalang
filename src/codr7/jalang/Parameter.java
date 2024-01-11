package codr7.jalang;

public record Parameter(String name, int rValue) {
  public String toString() {
    return name;
  }
}

package codr7.jalang;

public record Value<D>(Type<D> type, D data) {
  public String dump() {
    return type.dump(data);
  }

  public boolean isTrue() {
    return type.isTrue(data);
  }

  public String toString() {
    return type.dump(data);
  }
}

package codr7.jalang;

public record Value<D>(Type<D> type, D data) {
  public <D> D as(Type<D> type) {
    if (this.type != type) {
      throw new RuntimeException(String.format("Type mismatch: %s/%s", this.type, type));
    }

    return (D)data;
  }
  public String dump() {
    return type.dump(data);
  }

  public boolean isTrue() {
    return type.isTrue(data);
  }

  public String say() {
    return type.say(data);
  }

  public String toString() {
    return dump();
  }
}

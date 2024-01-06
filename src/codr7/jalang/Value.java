package codr7.jalang;

import codr7.jalang.libraries.Core;

public record Value<D>(Type<D> type, D data) implements Comparable<Value<?>> {
  public <D> D as(Type<D> type) {
    if (this.type != type) {
      throw new RuntimeException(String.format("Type mismatch: %s/%s", this.type, type));
    }

    return (D) data;
  }

  public int compareTo(final Value<?> other) {
    if (other.type != type) {
      throw new RuntimeException(String.format("Type mismatch: %s/%s.", type, other.type));
    }

    if (!(type instanceof Core.ComparableTrait)) {
      throw new RuntimeException(String.format("Type is not comparable: %s.", type));
    }

    return switch (((Core.ComparableTrait) type).compare(this, other)) {
      case Compare.LessThan -> -1;
      case Compare.GreaterThan -> 1;
      default -> 0;
    };
  }

  public String dump() {
    return type.dump(data);
  }

  public boolean equals(Value<?> other) {
    if (other.type() != type) {
      return false;
    }

    return type.equalValues(data, (D) other.data());
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

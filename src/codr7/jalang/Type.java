package codr7.jalang;

public class Type<D> {
  public static final Type<Type<?>> meta = new Type<>("Meta");

  public Type(final String name) {
    this.name = name;
  }

  public String dump(final D value) {
    return value.toString();
  }

  public final String name() {
    return name;
  }

  public boolean isTrue(final D value) {
    return true;
  }
  public String say(final D value) {
    return dump(value);
  }

  public final String toString() {
    return name;
  }

  private final String name;
}

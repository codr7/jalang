package codr7.jalang;

public class Type<D> {
  public Type(String name) {
    this.name = name;
  }

  public String dump(D value) {
    return value.toString();
  }

  public boolean isTrue(D value) {
    return true;
  }
  public String say(D value) {
    return dump(value);
  }
  public final String name;
}

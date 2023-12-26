package codr7.jalang;

public abstract class Form {
  public Form(final Location location) {
    this.location = location;
  }

  public abstract void emit(final Vm vm, final Namespace namespace, final int register);

  public final Location location() {
    return location;
  }

  public abstract String toString();

  private final Location location;
}

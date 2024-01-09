package codr7.jalang;

public abstract class Form {
  private final Location location;

  public Form(final Location location) {
    this.location = location;
  }

  public abstract void emit(final Vm vm, final Namespace namespace, final int register);

  public final Location location() {
    return location;
  }

  public abstract String toString();
}

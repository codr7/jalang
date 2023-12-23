package codr7.jalang;

public class Library extends Namespace {
  public Library(final String name, final Namespace parentNamespace) {
    super(parentNamespace);
    this.name = name;
  }
  public final String name;
}

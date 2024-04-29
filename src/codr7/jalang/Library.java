package codr7.jalang;

public class Library extends Namespace {
    private final String name;

    public Library(final String name, final Namespace parentNamespace) {
        super(parentNamespace);
        this.name = name;
    }

    public final String name() {
        return name;
    }
}

package codr7.jalang.forms;

import codr7.jalang.*;
import codr7.jalang.operations.Set;

public class LiteralForm extends Form {
    private final Value<?> value;

    public LiteralForm(final Location location, final Value<?> value) {
        super(location);
        this.value = value;
    }

    public void emit(final VM vm, final Namespace namespace, final int rResult) {
        vm.emit(new Set(rResult, value));
    }

    public String toString() {
        return value.toString();
    }

    public final Value<?> value() {
        return value;
    }
}

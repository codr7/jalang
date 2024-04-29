package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.VM;
import codr7.jalang.operations.Get;

public class RegisterForm extends Form {
    private final int index;

    public RegisterForm(final Location location, final int index) {
        super(location);
        this.index = index;
    }

    public void emit(final VM vm, final Namespace namespace, final int rResult) {
        vm.emit(new Get(index, rResult));
    }

    public String toString() {
        return String.format("r%d", index);
    }
}
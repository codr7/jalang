package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.VM;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Set;

public class NoneForm extends Form {
    public NoneForm(final Location location) {
        super(location);
    }

    public void emit(final VM vm, final Namespace namespace, final int rResult) {
        vm.emit(new Set(rResult, Core.NONE));
    }

    public String toString() {
        return "_";
    }
}

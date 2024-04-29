package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.VM;
import codr7.jalang.operations.MakeMap;
import codr7.jalang.operations.SetKey;

public class MapForm extends Form {
    private final Form[] body;

    public MapForm(final Location location, Form... body) {
        super(location);
        this.body = body;
    }

    public void emit(final VM vm, final Namespace namespace, final int rResult) {
        vm.emit(new MakeMap(rResult));
        final var rValue = vm.allocateRegister();

        for (final var f : body) {
            if (f instanceof PairForm pf) {
                final var rKey = vm.allocateRegister();
                pf.left().emit(vm, namespace, rKey);
                pf.right().emit(vm, namespace, rValue);
                vm.emit(new SetKey(rResult, rKey, rValue));
            } else {
                f.emit(vm, namespace, rValue);
                vm.emit(new SetKey(rResult, rValue, rValue));
            }
        }
    }

    public final Form[] body() {
        return body;
    }

    public String toString() {
        final var result = new StringBuilder();
        result.append('{');

        for (var i = 0; i < body.length; i++) {
            if (i > 0) {
                result.append(' ');
            }

            result.append(body[i].toString());
        }

        result.append('}');
        return result.toString();
    }
}

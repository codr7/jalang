package codr7.jalang.forms;

import codr7.jalang.Form;
import codr7.jalang.Location;
import codr7.jalang.Namespace;
import codr7.jalang.VM;
import codr7.jalang.operations.MakeVector;
import codr7.jalang.operations.Push;

public class VectorForm extends Form {
    private final Form[] body;

    public VectorForm(final Location location, Form... body) {
        super(location);
        this.body = body;
    }

    public void emit(final VM vm, final Namespace namespace, final int rResult) {
        vm.emit(new MakeVector(rResult));
        final var rItem = vm.allocateRegister();

        for (final var f : body) {
            f.emit(vm, namespace, rItem);
            vm.emit(new Push(rResult, rItem, rResult));
        }
    }

    public final Form[] body() {
        return body;
    }

    public String toString() {
        final var result = new StringBuilder();
        result.append('[');

        for (var i = 0; i < body.length; i++) {
            if (i > 0) {
                result.append(' ');
            }

            result.append(body[i].toString());
        }

        result.append(']');
        return result.toString();
    }
}
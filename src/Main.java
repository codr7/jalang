import codr7.jalang.Namespace;
import codr7.jalang.REPL;
import codr7.jalang.VM;
import codr7.jalang.compilers.GotoReturn;
import codr7.jalang.compilers.Unused;
import codr7.jalang.operations.Stop;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
    public static void main(final String[] args) throws IOException {
        final var vm = new VM();
        vm.addCompiler(GotoReturn.instance);
        vm.addCompiler(Unused.instance);

        final var namespace = new Namespace();
        namespace.include(vm.core);

        if (args.length > 0) {
            for (final var a : args) {
                final var startPc = vm.emitPc();
                vm.load(Paths.get(a), namespace);
                vm.emit(Stop.instance);
                vm.compile(startPc);
                vm.evaluate(startPc, namespace);
            }
        } else {
            System.out.printf("jalang v%d\n", VM.VERSION);
            System.out.println("May the source be with you!\n");
            final var repl = new REPL(vm, namespace, System.in, System.out);
            repl.run();
        }
    }
}
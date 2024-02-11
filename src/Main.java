import codr7.jalang.Namespace;
import codr7.jalang.Repl;
import codr7.jalang.Vm;
import codr7.jalang.compilers.GotoReturn;
import codr7.jalang.operations.Stop;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
  public static void main(final String[] args) throws IOException {
    final var vm = new Vm();
    vm.addCompiler(GotoReturn.instance);

    final var namespace = new Namespace();
    namespace.include(vm.core);

    if (args.length > 0) {
      for (final var a : args) {
        final var startPc = vm.emitPc();
        vm.load(Paths.get(a), namespace);
        vm.emit(Stop.instance);
        vm.evaluate(startPc, namespace);
      }
    } else {
      System.out.printf("jalang v%d\n", Vm.VERSION);
      System.out.println("May the source be with you!\n");
      final var repl = new Repl(vm, namespace, System.in, System.out);
      repl.run();
    }
  }
}
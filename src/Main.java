import codr7.jalang.Namespace;
import codr7.jalang.Repl;
import codr7.jalang.Vm;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {
  public static void main(final String[] args) throws IOException {
    final var vm = new Vm();
    final var namespace = new Namespace();
    namespace.include(vm.core);

    if (args.length > 0) {
      for (final var a : args) {
        vm.load(Paths.get(a), namespace, Vm.DEFAULT_REGISTER);
      }
    } else {
      System.out.printf("jalang v%d\n", Vm.VERSION);
      System.out.println("May the source be with you!\n");
      final var repl = new Repl(vm, namespace, System.in, System.out);
      repl.run();
    }
  }
}
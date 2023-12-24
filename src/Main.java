import codr7.jalang.Namespace;
import codr7.jalang.Repl;
import codr7.jalang.Vm;
import codr7.jalang.libraries.Core;
import codr7.jalang.operations.*;

import java.io.IOException;

public class Main {
  public static void main(final String[] args) throws IOException {
    final var vm = new Vm();
    vm.enableTracing(true);
    System.out.printf("jalang v%d\n", Vm.VERSION);
    System.out.println("May the source be with you!\n");
    final var repl = new Repl(vm, new Namespace(Core.instance), System.in, System.out);
    repl.run();
  }
}
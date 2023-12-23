import codr7.jalang.Repl;
import codr7.jalang.Vm;
import codr7.jalang.operations.*;

import java.io.IOException;

public class Main {
  public static void main(final String[] args) throws IOException {
    final var vm = new Vm();
    vm.enableTracing(true);
    final var repl = new Repl(System.in, System.out);
    repl.run(vm);
  }
}
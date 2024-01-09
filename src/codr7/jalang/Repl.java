package codr7.jalang;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.Stop;
import codr7.jalang.readers.FormReader;

import java.io.*;
import java.util.ArrayDeque;

public class Repl {
  private final BufferedReader in;
  private final Namespace namespace;
  private final PrintStream out;
  private final Vm vm;

  public Repl(final Vm vm, final Namespace namespace, final InputStream in, final PrintStream out) {
    this.vm = vm;
    this.namespace = namespace;
    this.in = new BufferedReader(new InputStreamReader(in));
    this.out = out;
  }

  public void evaluate(final String code) throws IOException {
    final var forms = new ArrayDeque<Form>();

    final var input = new Input(new StringReader(code));
    final var location = new Location("repl");
    while (FormReader.instance.read(input, forms, location)) ;

    var pc = vm.emitPc();

    for (final var f : forms) {
      f.emit(vm, namespace, Vm.DEFAULT_REGISTER);
    }

    vm.emit(Stop.instance);
    vm.evaluate(pc);
    var result = vm.get(Vm.DEFAULT_REGISTER);
    out.println((result == null) ? "_" : result.dump());
    vm.set(Vm.DEFAULT_REGISTER, Core.instance.NONE);
  }

  public void run() throws IOException {
    var inputBuffer = new StringBuilder();

    for (; ; ) {
      out.print("  ");
      final var line = in.readLine();

      if (line.isEmpty()) {
        try {
          evaluate(inputBuffer.toString());
        } catch (final Exception e) {
          out.println(e.getMessage());
        } finally {
          inputBuffer.setLength(0);
        }
      } else {
        inputBuffer.append(line);
        inputBuffer.append('\n');
      }
    }
  }
}

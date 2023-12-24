package codr7.jalang;

import codr7.jalang.operations.Stop;
import codr7.jalang.readers.FormReader;

import java.io.*;
import java.util.ArrayDeque;

public class Repl {
  public Repl(final Vm vm, final Namespace namespace, final InputStream in, final PrintStream out) {
    this.vm = vm;
    this.namespace = namespace;
    this.in = new BufferedReader(new InputStreamReader(in));
    this.out = out;
  }

  public void run() throws IOException {
    var inputBuffer = new StringBuilder();

    for (;;) {
      out.print("  ");
      final var line = in.readLine();

      if (line.isEmpty()) {
        final var forms = new ArrayDeque<Form>();

        try {
          final var input = new Input(new StringReader(inputBuffer.toString()));
          final var location = new Location("repl");
          while (FormReader.instance.read(input, forms, location));
        } finally {
          inputBuffer.setLength(0);
        }

        var pc = vm.emitPc();

        for (final var f: forms) {
          f.emit(vm, namespace, Vm.DEFAULT_REGISTER);
        }

        vm.emit(Stop.instance);
        vm.evaluate(pc);
        var result = vm.peek(Vm.DEFAULT_REGISTER);
        out.println((result == null) ? "_" : result.dump());
      } else {
        inputBuffer.append(line);
        inputBuffer.append('\n');
      }
    }
  }

  private final BufferedReader in;
  private final Namespace namespace;
  private final PrintStream out;
  private final Vm vm;
}

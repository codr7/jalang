package codr7.jalang;

import codr7.jalang.operations.Stop;

import java.io.*;

public class Repl {
  public Repl(final InputStream in, final PrintStream out) {
    this.in = new BufferedReader(new InputStreamReader(in));
    this.out = out;
  }

  public void run(final Vm vm) throws IOException {
    var buffer = new StringBuilder();

    for (;;) {
      out.print("  ");
      final var line = in.readLine();

      if (line.isEmpty()) {
        buffer.setLength(0);
        var pc = vm.emitPc();
        vm.emit(Stop.instance);
        vm.evaluate(pc);
        var result = vm.peek(0);
        out.println((result == null) ? "_" : result.dump());
      } else {
        buffer.append(line);
        buffer.append('\n');
      }
    }
  }

  private final BufferedReader in;
  private final PrintStream out;
}

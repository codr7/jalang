package codr7.jalang;

import codr7.jalang.libraries.Core;

import java.io.*;

public class REPL {
    private final BufferedReader in;
    private final Namespace namespace;
    private final PrintStream out;
    private final VM vm;

    public REPL(final VM vm, final Namespace namespace, final InputStream in, final PrintStream out) {
        this.vm = vm;
        this.namespace = namespace;
        this.in = new BufferedReader(new InputStreamReader(in));
        this.out = out;
    }

    public void run() throws IOException {
        var inputBuffer = new StringBuilder();

        for (; ; ) {
            out.print("  ");
            final var line = in.readLine();

            if (line.isEmpty()) {
                try {
                    vm.evaluate(inputBuffer.toString(), namespace);
                    var result = vm.get(VM.DEFAULT_REGISTER);
                    out.println((result == null) ? "_" : result.dump());
                    vm.set(VM.DEFAULT_REGISTER, Core.NONE);
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

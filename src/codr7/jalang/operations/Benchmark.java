package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Benchmark extends Operation {
  public Benchmark(final int repetitions, final int register) {
    super(Code.Benchmark);
    this.repetitions = repetitions;
    this.register = register;
  }

  public String toString() {
    return String.format("%s repetitions: %d register: %d", super.toString(), repetitions, register);
  }

  public final int repetitions;
  public final int register;
}
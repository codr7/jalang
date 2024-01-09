package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Benchmark extends Operation {
  public final int rRepetitions;
  public final int rRegister;

  public Benchmark(final int rRepetitions, final int rResult) {
    super(Code.Benchmark);
    this.rRepetitions = rRepetitions;
    this.rRegister = rResult;
  }

  public String toString() {
    return String.format("%s repetitions: %d result: %d", super.toString(), rRepetitions, rRegister);
  }
}
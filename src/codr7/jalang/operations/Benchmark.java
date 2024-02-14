package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.util.Set;

public class Benchmark extends Operation {
  public final int rRepetitions;
  public final int rResult;

  public Benchmark(final int rRepetitions, final int rResult) {
    super(Code.Benchmark);
    this.rRepetitions = rRepetitions;
    this.rResult = rResult;
  }

  public void addReads(final java.util.Set<Integer> out) {
    out.add(rRepetitions);
  }

  public void addWrites(final Set<Integer> out) {
    out.add(rResult);
  }

  public String toString() {
    return String.format("%s repetitions: %d result: %d", super.toString(), rRepetitions, rResult);
  }
}
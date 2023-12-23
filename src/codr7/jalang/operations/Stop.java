package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Stop extends Operation {
  public static final Stop instance = new Stop();

  public Stop() {
    super(Code.Stop);
  }
}


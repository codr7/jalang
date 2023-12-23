package codr7.jalang.operations;

import codr7.jalang.Operation;

public class MakePair extends Operation {
  public MakePair(final int left, final int right, final int result) {
    super(Code.MakePair);
    this.left = left;
    this.right = right;
    this.result = result;
  }
  public final int left, right, result;
}

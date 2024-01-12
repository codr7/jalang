package codr7.jalang;

import codr7.jalang.errors.EvaluationError;
import codr7.jalang.libraries.Core;

public record MacroReference(Macro macro, int startPc, int[] rParameters) {
  public void call(final Vm vm, final Location location, final int[] rParameters, final int rResult) {
    if (this.rParameters.length != rParameters.length) {
      throw new EvaluationError(location, "Wrong number of macro reference arguments.");
    }

    for (int i = 0; i < rParameters.length; i++) {
      vm.set(this.rParameters[i], vm.get(rParameters[i]));
    }

    vm.pushCall(new Value<>(Core.macroReferenceType, this), location, startPc, rResult);
  }

  public String toString() {
    return String.format("(Macro %s-%d)", macro.name(), rParameters.length);
  }
}

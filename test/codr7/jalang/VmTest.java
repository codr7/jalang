package codr7.jalang;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.MakePair;
import codr7.jalang.operations.Set;
import codr7.jalang.operations.Stop;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VmTest {
  @Test
  public void testPair() {
    final var vm = new Vm();
    final var left = new Value<Integer>(Core.instance.integerType, 1);
    final var right = new Value<Integer>(Core.instance.integerType, 2);
    vm.emit(new Set(left, 1));
    vm.emit(new Set(right, 2));
    vm.emit(new MakePair(1, 2, 3));
    vm.emit(Stop.instance);
    vm.evaluate(0);
    assertEquals(new Value<Pair>(Core.instance.pairType, new Pair(left, right)), vm.get(3));
  }
}
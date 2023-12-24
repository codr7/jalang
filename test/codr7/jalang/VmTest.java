package codr7.jalang;

import codr7.jalang.libraries.Core;
import codr7.jalang.operations.MakePair;
import codr7.jalang.operations.Poke;
import codr7.jalang.operations.Stop;
import codr7.jalang.types.Pair;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VmTest {
  @Test
  public void testPair() {
    final var vm = new Vm();
    final var left = new Value<Integer>(Core.instance.intType, 1);
    final var right = new Value<Integer>(Core.instance.intType, 2);
    vm.emit(new Poke(1, left));
    vm.emit(new Poke(2, right));
    vm.emit(new MakePair(1, 2, 3));
    vm.emit(Stop.instance);
    vm.evaluate(0);
    assertEquals(new Value<Pair>(Core.instance.pairType, new Pair(left, right)), vm.peek(3));
  }
}
import codr7.jalang.VM;
import codr7.jalang.operations.Stop;

public class Main {
  public static void main(final String[] args) {
    final var vm = new VM();
    vm.enableTracing(true);
    vm.emit(Stop.instance);
    vm.evaluate(0);
  }
}
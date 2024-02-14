package codr7.jalang;

public interface Compiler {
  boolean compile(Operation[] code, int startPc);
}

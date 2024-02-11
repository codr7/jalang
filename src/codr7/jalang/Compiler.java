package codr7.jalang;

import java.util.ArrayList;

public interface Compiler {
  boolean compile(Operation[] code, int startPc);
}

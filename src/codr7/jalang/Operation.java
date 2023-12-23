package codr7.jalang;

public abstract class Operation {
  public enum Code {
    Goto, Nop, Stop
  }

  public static class Goto extends Operation {
    public Goto(final int pc) {
      super(Code.Goto);
      this.pc = pc;
    }
    public final int pc;
  }

  public static class Nop extends Operation {
    public Nop() {
      super(Code.Nop);
    }
  }

  public static class Stop extends Operation {
    public Stop() {
      super(Code.Stop);
    }
  }

  public Operation(final Code code) {
    this.code = code;
  }

  public final Code code;
}

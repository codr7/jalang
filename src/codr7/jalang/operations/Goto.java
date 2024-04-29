package codr7.jalang.operations;

import codr7.jalang.Operation;

public class Goto extends Operation {
    public final int pc;

    public Goto(final int pc) {
        super(Code.Goto);
        this.pc = pc;
    }

    public String toString() {
        return String.format("%s pc: %d", super.toString(), pc);
    }
}

package codr7.jalang.operations;

import codr7.jalang.Operation;

public class NotImplemented extends Operation {
    public static final NotImplemented instance = new NotImplemented();

    public NotImplemented() {
        super(Code.NotImplemented);
    }
}

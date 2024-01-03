package codr7.jalang;

public record CallFrame(CallFrame parentFrame,
                        Function target,
                        Location location,
                        Value<?>[] registers,
                        int returnPc,
                        int rResult) {
}

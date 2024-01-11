package codr7.jalang;

public record CallFrame(CallFrame parentFrame,
                        Value<?> target,
                        Location location,
                        Value<?>[] registers,
                        int returnPc,
                        int rResult) {
}

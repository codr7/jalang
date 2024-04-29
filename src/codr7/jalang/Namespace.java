package codr7.jalang;

import codr7.jalang.libraries.Core;

import java.util.HashMap;
import java.util.Map;

public class Namespace {
    private final Namespace parentNamespace;
    private final Map<String, Value<?>> bindings = new HashMap<>();

    public Namespace(final Namespace parentNamespace) {
        this.parentNamespace = parentNamespace;
    }

    public Namespace() {
        this(null);
    }

    public final void alias(final String targetKey, final String sourceKey) {
        bind(targetKey, find(sourceKey));
    }

    public final void bind(final String key, final Value<?> value) {
        bindings.put(key, value);
    }

    public final void bindFunction(final String name,
                                   final String[] parameterNames,
                                   final Function.Body body) {
        final var parameters = new Function.Parameter[parameterNames.length];

        for (int i = 0; i < parameterNames.length; i++) {
            parameters[i] = new Function.Parameter(parameterNames[i], -1);
        }

        bind(name, new Value<>(Core.functionType, new Function(name, parameters, body)));
    }

    public final void bindMacro(final String name, final int arity, final Macro.Body body) {
        bind(name, new Value<>(Core.macroType, new Macro(name, arity, body)));
    }

    public final void bindType(final Type<?> type) {
        bind(type.name(), new Value<>(Core.metaType, type));
    }

    public final Value<?> find(final String key) {
        var v = bindings.get(key);

        if (v == null && parentNamespace != null) {
            return parentNamespace.find(key);
        }

        return v;
    }

    public void evaluate(final VM vm, final String code) {
        vm.evaluate(code, this);
    }

    public final void include(final Namespace source) {
        bindings.putAll(source.bindings);
    }
}

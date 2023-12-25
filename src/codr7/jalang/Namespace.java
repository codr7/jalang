package codr7.jalang;

import java.util.HashMap;
import java.util.Map;

public class Namespace {
  public Namespace(final Namespace parentNamespace) {
    this.parentNamespace = parentNamespace;
  }

  public final void bind(final String key, final Value<?> value) {
    bindings.put(key, value);
  }

  public final void bindFunction(final String name, final int arity, final Function.Body body) {
    bind(name, new Value<>(Function.type, new Function(name, arity, body)));
  }

  public final void bindMacro(final String name, final int arity, final Macro.Body body) {
    bind(name, new Value<>(Macro.type, new Macro(name, arity, body)));
  }

  public final void bindType(final Type<?> type) {
    bind(type.name(), new Value<>(Type.meta, type));
  }

  public final Value<?> find(final String key) {
    var v = bindings.get(key);

    if (v == null && parentNamespace != null) {
      return parentNamespace.find(key);
    }

    return v;
  }

  private final Namespace parentNamespace;
  private final Map<String, Value> bindings = new HashMap<>();
}

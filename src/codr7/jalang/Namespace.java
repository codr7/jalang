package codr7.jalang;

import java.util.HashMap;
import java.util.Map;

public class Namespace {
  public Namespace(final Namespace parentNamespace) {
    this.parentNamespace = parentNamespace;
  }

  public final Value<?> get(final String key) {
    var v = bindings.get(key);

    if (v == null && parentNamespace != null) {
      return parentNamespace.get(key);
    }

    return v;
  }

  public final void put(final String key, final Value<?> value) {
    bindings.put(key, value);
  }

  private final Namespace parentNamespace;
  private final Map<String, Value> bindings = new HashMap<>();
}

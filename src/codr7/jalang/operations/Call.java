package codr7.jalang.operations;

import codr7.jalang.Function;
import codr7.jalang.Location;
import codr7.jalang.Operation;

public class Call extends Operation {
  public Call(final Function target, final int arity, final int register, final Location location) {
    super(Code.Call);
    this.target = target;
    this.arity = arity;
    this.register = register;
    this.location = location;
  }

  public final int arity;
  public final Location location;
  public final int register;
  public final Function target;
}

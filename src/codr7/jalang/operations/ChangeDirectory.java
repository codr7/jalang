package codr7.jalang.operations;

import codr7.jalang.Operation;

import java.nio.file.Path;

public class ChangeDirectory extends Operation {
  public final Path path;

  public ChangeDirectory(final Path path) {
    super(Code.ChangeDirectory);
    this.path = path;
  }

  public String toString() {
    return String.format("%s path: %s", super.toString(), path);
  }
}
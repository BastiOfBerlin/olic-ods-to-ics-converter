package de.bastiofberlin.olicodstoicsconverter;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Objects;

public final class TestFiles {
  private TestFiles() {}

  /** Resolves a classpath resource (e.g. {@code /odf/basic.ods}) to a file path. */
  public static Path resource(final String name) {
    try {
      return Path.of(Objects.requireNonNull(TestFiles.class.getResource(name), name).toURI());
    } catch (final URISyntaxException e) {
      throw new IllegalStateException(e);
    }
  }
}

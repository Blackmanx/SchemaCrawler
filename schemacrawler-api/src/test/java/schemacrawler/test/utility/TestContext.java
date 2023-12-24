/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2024, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/

package schemacrawler.test.utility;

import static java.nio.file.Files.createDirectories;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ToStringBuilder;

public final class TestContext {

  private static Object nullSafeGet(final Optional<?> optional) {
    return optional != null ? optional.orElse(null) : null;
  }

  private final Optional<Class<?>> optionalTestClass;
  private final Optional<Method> optionalTestMethod;

  TestContext(final ExtensionContext extensionContext) {
    optionalTestClass = extensionContext.getTestClass();
    optionalTestMethod = extensionContext.getTestMethod();
  }

  public Path resolveTargetFromRootPath(final String relativePath)
      throws URISyntaxException, IOException {
    final Path projectRootPath = getProjectRootPath();
    final Path directory =
        projectRootPath
            .resolve(Paths.get("target"))
            .resolve(relativePath)
            .normalize()
            .toAbsolutePath();
    createDirectories(directory);
    return directory;
  }

  public String testMethodFullName() {
    return optionalTestMethod
        .map(
            method ->
                String.format(
                    "%s.%s", method.getDeclaringClass().getSimpleName(), method.getName()))
        .orElseThrow(() -> new RuntimeException("Could not find test method"));
  }

  public String testMethodName() {
    return optionalTestMethod
        .map(Method::getName)
        .orElseThrow(() -> new RuntimeException("Could not find test method"));
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this)
        .append("testClass", nullSafeGet(optionalTestClass))
        .append("testMethod", nullSafeGet(optionalTestMethod))
        .toString();
  }

  private Path getProjectRootPath() throws URISyntaxException, IOException {
    final Class<?> testClass =
        optionalTestClass.orElseThrow(() -> new RuntimeException("Could not find test class"));
    final Path codePath =
        Paths.get(testClass.getProtectionDomain().getCodeSource().getLocation().toURI())
            .normalize()
            .toAbsolutePath();
    final Path projectRoot = codePath.resolve("../..").normalize().toAbsolutePath();
    Files.createDirectories(projectRoot);
    return projectRoot;
  }
}

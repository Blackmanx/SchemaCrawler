/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2022, Sualeh Fatehi <sualeh@hotmail.com>.
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

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;

import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Optional;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestWithSystemProperty implements BeforeEachCallback, AfterEachCallback {

  private SimpleImmutableEntry<String, String> systemProperty;

  @Override
  public void afterEach(final ExtensionContext context) throws Exception {
    System.clearProperty(systemProperty.getKey());
    systemProperty = null;
  }

  @Override
  public void beforeEach(final ExtensionContext context) throws Exception {
    final Optional<WithSystemProperty> optionalAnnotation =
        findAnnotation(context.getTestMethod(), WithSystemProperty.class);
    optionalAnnotation.ifPresent(
        withSystemProperty -> {
          systemProperty =
              new SimpleImmutableEntry<>(withSystemProperty.key(), withSystemProperty.value());
          System.setProperty(systemProperty.getKey(), systemProperty.getValue());
        });
  }
}

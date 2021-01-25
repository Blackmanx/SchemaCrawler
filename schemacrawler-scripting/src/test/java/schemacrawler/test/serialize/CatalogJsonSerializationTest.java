/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2021, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.test.serialize;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.oneOf;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.TestUtility.fileHeaderOf;
import static schemacrawler.tools.utility.SchemaCrawlerUtility.getCatalog;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.TestContext;
import schemacrawler.test.utility.TestContextParameterResolver;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.test.utility.TestWriter;
import schemacrawler.tools.formatter.serialize.JsonSerializedCatalog;
import us.fatehi.utility.IOUtility;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
public class CatalogJsonSerializationTest {

  private static final boolean DEBUG = true;
  private Path directory;

  @BeforeEach
  public void _setupDirectory(final TestContext testContext)
      throws IOException, URISyntaxException {
    if (directory != null) {
      return;
    }
    directory = testContext.resolveTargetFromRootPath(".");
  }

  @Test
  public void catalogSerializationWithJson(
      final TestContext testContext, final Connection connection) throws Exception {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);

    final Path testOutputFile = IOUtility.createTempFilePath("sc_serialized_catalog", "json");
    try (final OutputStream out = new FileOutputStream(testOutputFile.toFile())) {
      new JsonSerializedCatalog(catalog).save(out);
    }
    assertThat("Catalog was not serialized", Files.size(testOutputFile), greaterThan(0L));
    assertThat(fileHeaderOf(testOutputFile), is(oneOf("7B0D", "7B0A")));

    if (DEBUG) {
      final Path copied = directory.resolve(testContext.testMethodFullName() + ".json");
      Files.copy(testOutputFile, copied, StandardCopyOption.REPLACE_EXISTING);
    }

    // Read generated JSON file, and assert values
    final ObjectMapper objectMapper = new ObjectMapper();
    final JsonNode catalogNode = objectMapper.readTree(testOutputFile.toFile());
    assertThat(
        "Catalog schemas were not serialized",
        catalogNode.findPath("schemas"),
        not(instanceOf(MissingNode.class)));

    final JsonNode allTableColumnsNode = catalogNode.findPath("all-table-columns");
    assertThat(
        "Table columns were not serialized",
        allTableColumnsNode,
        not(instanceOf(MissingNode.class)));

    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      allTableColumnsNode
          .elements()
          .forEachRemaining(
              columnNode -> {
                final JsonNode columnFullnameNode = columnNode.get("full-name");
                if (columnFullnameNode != null) {
                  out.println("- column @uuid: " + columnNode.get("@uuid").asText());
                  out.println("  " + columnFullnameNode.asText());
                } else {
                  fail("Table column object not found - " + columnNode.asText());
                }
              });
    }

    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(testContext.testMethodFullName())));
  }
}

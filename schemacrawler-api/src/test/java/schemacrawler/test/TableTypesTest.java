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

package schemacrawler.test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.arrayWithSize;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;

import java.sql.Connection;
import java.util.Arrays;

import org.junit.jupiter.api.Test;

import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.TestWriter;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.utility.NamedObjectSort;

@WithTestDatabase
public class TableTypesTest {

  private static final String TABLE_TYPES_OUTPUT = "table_types/";

  @Test
  public void all(final Connection connection) throws Exception {
    test(connection, "all.txt", null);
  }

  @Test
  public void bad(final Connection connection) throws Exception {
    test(connection, "bad.txt", "BAD TABLE TYPE");
  }

  @Test
  public void defaultTableTypes(final Connection connection) throws Exception {
    test(connection, "default.txt", "default");
  }

  @Test
  public void global_temporary(final Connection connection) throws Exception {
    test(connection, "global_temporary.txt", "GLOBAL TEMPORARY");
  }

  @Test
  public void mixed(final Connection connection) throws Exception {
    test(connection, "mixed.txt", " global temporary, view ");
  }

  @Test
  public void none(final Connection connection) throws Exception {
    test(connection, "none.txt", "");
  }

  @Test
  public void system(final Connection connection) throws Exception {
    test(connection, "system.txt", "SYSTEM TABLE");
  }

  @Test
  public void tables(final Connection connection) throws Exception {
    test(connection, "tables.txt", "TABLE");
  }

  @Test
  public void views(final Connection connection) throws Exception {
    test(connection, "views.txt", "VIEW");
  }

  private void test(
      final Connection connection, final String referenceFile, final String tableTypes)
      throws Exception {
    final TestWriter testout = new TestWriter();
    try (final TestWriter out = testout) {
      final LimitOptionsBuilder limitOptionsBuilder =
          LimitOptionsBuilder.builder()
              .includeSchemas(new RegularExpressionExclusionRule(".*\\.FOR_LINT"));
      if (!"default".equals(tableTypes)) {
        limitOptionsBuilder.tableTypes(tableTypes);
      }
      final SchemaCrawlerOptions schemaCrawlerOptions =
          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
              .withLimitOptions(limitOptionsBuilder.toOptions());

      final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
      final Schema[] schemas = catalog.getSchemas().toArray(new Schema[0]);
      assertThat("Schema count does not match", schemas, arrayWithSize(5));
      for (final Schema schema : schemas) {
        out.println(String.format("%s", schema.getFullName()));
        final Table[] tables = catalog.getTables(schema).toArray(new Table[0]);
        Arrays.sort(tables, NamedObjectSort.alphabetical);
        for (final Table table : tables) {
          out.println(String.format("  %s [%s]", table.getName(), table.getTableType()));
          final Column[] columns = table.getColumns().toArray(new Column[0]);
          Arrays.sort(columns);
          for (final Column column : columns) {
            out.println(String.format("    %s [%s]", column.getName(), column.getColumnDataType()));
          }
        }
      }
    }
    assertThat(
        outputOf(testout), hasSameContentAs(classpathResource(TABLE_TYPES_OUTPUT + referenceFile)));
  }
}

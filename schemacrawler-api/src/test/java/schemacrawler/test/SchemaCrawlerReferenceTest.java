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

package schemacrawler.test;

import static com.github.npathai.hamcrestopt.OptionalMatchers.isEmpty;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.fail;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;

import java.sql.Connection;
import java.util.Collection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.crawl.NotLoadedException;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.DatabaseTestUtility;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class SchemaCrawlerReferenceTest {

  @Test
  public void fkReferences(final Connection connection) throws Exception {

    final SchemaCrawlerOptions schemaCrawlerOptions =
        DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

    int fkReferenceCount = 0;
    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    final Collection<Table> tables = catalog.getTables();
    for (final Table table : tables) {
      final Collection<ForeignKey> foreignKeys = table.getForeignKeys();
      for (final ForeignKey foreignKey : foreignKeys) {
        for (final ColumnReference fkColumnRef : foreignKey) {
          assertReferencedColumnExists(catalog, fkColumnRef.getPrimaryKeyColumn());
          assertReferencedColumnExists(catalog, fkColumnRef.getForeignKeyColumn());

          fkReferenceCount++;
        }
      }
    }

    assertThat(fkReferenceCount, is(26));
  }

  @Test
  public void fkReferencesForGreppedAndFilteredTables1(final Connection connection)
      throws Exception {
    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeTables(new RegularExpressionInclusionRule(".*\\.BOOKAUTHORS"));
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.BOOKAUTHORS\\..*"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions())
            .withGrepOptions(grepOptionsBuilder.toOptions());

    int fkReferenceCount = 0;
    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    final Collection<Table> tables = catalog.getTables();
    for (final Table table : tables) {
      final Collection<ForeignKey> foreignKeys = table.getForeignKeys();
      for (final ForeignKey foreignKey : foreignKeys) {
        for (final ColumnReference fkColumnRef : foreignKey) {
          assertReferencedColumnDoesNotExist(catalog, fkColumnRef.getPrimaryKeyColumn(), true);
          assertReferencedColumnExists(catalog, fkColumnRef.getForeignKeyColumn());

          fkReferenceCount++;
        }
      }
    }

    assertThat(fkReferenceCount, is(2));
  }

  @Test
  public void fkReferencesForGreppedAndFilteredTables2(final Connection connection)
      throws Exception {
    final LimitOptionsBuilder limitOptionsBuilder =
        LimitOptionsBuilder.builder()
            .includeTables(new RegularExpressionInclusionRule(".*\\.AUTHORS"));
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.AUTHORS\\..*"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withLimitOptions(limitOptionsBuilder.toOptions())
            .withGrepOptions(grepOptionsBuilder.toOptions());

    int fkReferenceCount = 0;
    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    final Collection<Table> tables = catalog.getTables();
    for (final Table table : tables) {
      final Collection<ForeignKey> foreignKeys = table.getForeignKeys();
      for (final ForeignKey foreignKey : foreignKeys) {
        for (final ColumnReference fkColumnRef : foreignKey) {
          assertReferencedColumnExists(catalog, fkColumnRef.getPrimaryKeyColumn());
          assertReferencedColumnDoesNotExist(catalog, fkColumnRef.getForeignKeyColumn(), true);

          fkReferenceCount++;
        }
      }
    }

    assertThat(fkReferenceCount, is(1));
  }

  @Test
  public void fkReferencesForGreppedTables1(final Connection connection) throws Exception {
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.BOOKAUTHORS\\..*"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withGrepOptions(grepOptionsBuilder.toOptions());

    int fkReferenceCount = 0;
    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    final Collection<Table> tables = catalog.getTables();
    for (final Table table : tables) {
      final Collection<ForeignKey> foreignKeys = table.getForeignKeys();
      for (final ForeignKey foreignKey : foreignKeys) {
        for (final ColumnReference fkColumnRef : foreignKey) {
          assertReferencedColumnDoesNotExist(catalog, fkColumnRef.getPrimaryKeyColumn(), false);
          assertReferencedColumnExists(catalog, fkColumnRef.getForeignKeyColumn());

          fkReferenceCount++;
        }
      }
    }

    assertThat(fkReferenceCount, is(2));
  }

  @Test
  public void fkReferencesForGreppedTables2(final Connection connection) throws Exception {
    final GrepOptionsBuilder grepOptionsBuilder =
        GrepOptionsBuilder.builder()
            .includeGreppedColumns(new RegularExpressionInclusionRule(".*\\.AUTHORS\\..*"));
    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withGrepOptions(grepOptionsBuilder.toOptions());

    int fkReferenceCount = 0;
    final Catalog catalog = getCatalog(connection, schemaCrawlerOptions);
    final Collection<Table> tables = catalog.getTables();
    for (final Table table : tables) {
      final Collection<ForeignKey> foreignKeys = table.getForeignKeys();
      for (final ForeignKey foreignKey : foreignKeys) {
        for (final ColumnReference fkColumnRef : foreignKey) {
          assertReferencedColumnExists(catalog, fkColumnRef.getPrimaryKeyColumn());
          assertReferencedColumnDoesNotExist(catalog, fkColumnRef.getForeignKeyColumn(), false);

          fkReferenceCount++;
        }
      }
    }

    assertThat(fkReferenceCount, is(1));
  }

  private void assertReferencedColumnDoesNotExist(
      final Catalog catalog, final Column column, final boolean assertDataNotLoaded) {
    final Table table = column.getParent();
    assertThat(
        "Primary key table table should not be in the database - " + table.getName(),
        catalog.lookupTable(table.getSchema(), table.getName()),
        isEmpty());
    assertThat(
        "Column references do not match",
        column == table.lookupColumn(column.getName()).get(),
        is(true));

    if (assertDataNotLoaded) {
      try {
        table.getTableType();
        fail(
            "An exception should be thrown indicating that this table was not loaded from the database");
      } catch (final NotLoadedException e) {
        // Expected exception
      }
      try {
        column.getColumnDataType();
        fail(
            "An exception should be thrown indicating that this table was not loaded from the database");
      } catch (final NotLoadedException e) {
        // Expected exception
      }
    }
  }

  private void assertReferencedColumnExists(final Catalog catalog, final Column column) {
    assertThat(column, notNullValue());
    final Table table = column.getParent();
    assertThat(
        "Table references do not match - " + table.getName(),
        table == catalog.lookupTable(table.getSchema(), table.getName()).get());
    assertThat(
        "Column references do not match", column == table.lookupColumn(column.getName()).get());
  }
}

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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static schemacrawler.test.utility.DatabaseTestUtility.getCatalog;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

import java.sql.Connection;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schema.Catalog;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.Index;
import schemacrawler.schema.PrimaryKey;
import schemacrawler.schema.Schema;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.IdentifierQuotingStrategy;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.utility.MetaDataUtility;
import schemacrawler.utility.MetaDataUtility.ForeignKeyCardinality;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@TestInstance(Lifecycle.PER_CLASS)
public class MetadataUtilityTest {

  private Catalog catalog;

  @Test
  public void columnsListAsStringConstraint() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final PrimaryKey pk = table.getPrimaryKey();
    assertThat("Index not found", pk, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(pk, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @Test
  public void columnsListAsStringFk() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final ForeignKey fk = table.getForeignKeys().toArray(new ForeignKey[0])[0];
    assertThat("Foreign key not found", fk, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(
            fk, TableRelationshipType.child, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(columnsListAsStringChild, is("'PREVIOUSEDITIONID'"));

    final String columnsListAsStringParent =
        MetaDataUtility.getColumnsListAsString(
            fk, TableRelationshipType.parent, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(columnsListAsStringParent, is("'ID'"));

    final String columnsListAsStringNone =
        MetaDataUtility.getColumnsListAsString(
            fk, TableRelationshipType.none, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(columnsListAsStringNone, is(""));
  }

  @Test
  public void columnsListAsStringIndex() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final Index index = table.getIndexes().toArray(new Index[0])[0];
    assertThat("Index not found", index, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(index, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(columnsListAsStringChild, is("'ID'"));
  }

  @Test
  public void columnsListAsStringTable() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final String columnsListAsStringChild =
        MetaDataUtility.getColumnsListAsString(table, IdentifierQuotingStrategy.quote_all, "'");
    assertThat(
        columnsListAsStringChild,
        is(
            "'ID', 'TITLE', 'DESCRIPTION', 'PUBLISHERID', 'PUBLICATIONDATE', 'PRICE', 'PREVIOUSEDITIONID'"));
  }

  @Test
  public void fkUtilities() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    final ForeignKey fk = table.getForeignKeys().toArray(new ForeignKey[0])[0];
    assertThat("Foreign key not found", fk, notNullValue());

    final ColumnReference columnReference =
        fk.getColumnReferences().toArray(new ColumnReference[0])[0];
    assertThat("Column reference not found", columnReference, notNullValue());

    assertThat(MetaDataUtility.findForeignKeyCardinality(fk), is(ForeignKeyCardinality.zero_one));

    assertThat(
        MetaDataUtility.foreignKeyColumnNames(fk),
        containsInAnyOrder("PUBLIC.BOOKS.BOOKS.PREVIOUSEDITIONID"));
  }

  @BeforeAll
  public void loadCatalog(final Connection connection) throws SchemaCrawlerException {
    final SchemaCrawlerOptions schemaCrawlerOptions =
        schemaCrawlerOptionsWithMaximumSchemaInfoLevel;

    catalog = getCatalog(connection, schemaCrawlerOptions);
  }

  @Test
  public void tableUtilities() throws Exception {

    final Schema schema = catalog.lookupSchema("PUBLIC.BOOKS").get();
    assertThat("BOOKS Schema not found", schema, notNullValue());

    final Table table = catalog.lookupTable(schema, "BOOKS").get();
    assertThat("BOOKS Table not found", table, notNullValue());

    assertThat(
        MetaDataUtility.allIndexCoumnNames(table)
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet()),
        containsInAnyOrder("PUBLIC.BOOKS.BOOKS.ID", "PUBLIC.BOOKS.BOOKS.PREVIOUSEDITIONID"));

    assertThat(
        MetaDataUtility.uniqueIndexCoumnNames(table)
            .stream()
            .flatMap(List::stream)
            .collect(Collectors.toSet()),
        containsInAnyOrder("PUBLIC.BOOKS.BOOKS.ID", "PUBLIC.BOOKS.BOOKS.PREVIOUSEDITIONID"));

    final Index index = table.getIndexes().toArray(new Index[0])[0];
    assertThat("Index not found", index, notNullValue());

    assertThat(MetaDataUtility.columnNames(index), containsInAnyOrder("PUBLIC.BOOKS.BOOKS.ID"));
  }
}

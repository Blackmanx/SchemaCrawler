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

package schemacrawler.utility;

import static java.util.Objects.requireNonNull;
import static schemacrawler.schemacrawler.Identifiers.identifiers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.Index;
import schemacrawler.schema.IndexColumn;
import schemacrawler.schema.JavaSqlTypeGroup;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.TableConstraint;
import schemacrawler.schema.TableConstraintColumn;
import schemacrawler.schema.TableReference;
import schemacrawler.schema.TableRelationshipType;
import schemacrawler.schemacrawler.IdentifierQuotingStrategy;
import schemacrawler.schemacrawler.Identifiers;
import us.fatehi.utility.UtilityMarker;

/**
 * SchemaCrawler utility methods.
 *
 * @author sfatehi
 */
@UtilityMarker
public final class MetaDataUtility {

  public enum ForeignKeyCardinality {
    unknown(""),
    zero_one("(0..1)"),
    zero_many("(0..many)"),
    one_one("(1..1)");

    private final String description;

    ForeignKeyCardinality(final String description) {
      this.description = requireNonNull(description, "No description provided");
    }

    @Override
    public String toString() {
      return description;
    }
  }

  public static Collection<List<String>> allIndexCoumnNames(final Table table) {
    return indexCoumnNames(table, false);
  }

  public static List<String> columnNames(final Index index) {
    if (index == null) {
      return Collections.emptyList();
    }

    final List<String> columnNames = new ArrayList<>();
    for (final Column indexColumn : index) {
      columnNames.add(indexColumn.getFullName());
    }
    return columnNames;
  }

  public static ForeignKeyCardinality findForeignKeyCardinality(final TableReference foreignKey) {
    if (foreignKey == null) {
      return ForeignKeyCardinality.unknown;
    }
    final boolean isForeignKeyUnique = isForeignKeyUnique(foreignKey);

    final ColumnReference columnRef0 = foreignKey.getColumnReferences().get(0);
    final Column fkColumn = columnRef0.getForeignKeyColumn();
    final boolean isColumnReference = fkColumn instanceof PartialDatabaseObject;

    final ForeignKeyCardinality connectivity;
    if (isColumnReference) {
      connectivity = ForeignKeyCardinality.unknown;
    } else if (isForeignKeyUnique) {
      connectivity = ForeignKeyCardinality.zero_one;
    } else {
      connectivity = ForeignKeyCardinality.zero_many;
    }
    return connectivity;
  }

  public static List<String> foreignKeyColumnNames(final TableReference foreignKey) {
    if (foreignKey == null) {
      return Collections.emptyList();
    }
    final List<String> columnNames = new ArrayList<>();
    for (final ColumnReference columnReference : foreignKey) {
      columnNames.add(columnReference.getForeignKeyColumn().getFullName());
    }
    return columnNames;
  }

  /**
   * Gets a comma-separated list of columns for an index.
   *
   * @param index Index
   * @param quotingStrategy Identifier quoting strategy
   * @param quoteString
   * @return Comma-separated list of columns
   */
  public static String getColumnsListAsString(
      final Index index,
      final IdentifierQuotingStrategy quotingStrategy,
      final String quoteString) {

    requireNonNull(index, "No index provided");
    requireNonNull(quotingStrategy, "No identifier quoting strategy provided");

    final List<IndexColumn> columns = index.getColumns();
    return joinColumns(quotingStrategy, quoteString, columns);
  }

  /**
   * Gets a comma-separated list of columns for a table.
   *
   * @param table Table
   * @param quotingStrategy Identifier quoting strategy
   * @param quoteString
   * @return Comma-separated list of columns
   */
  public static String getColumnsListAsString(
      final Table table,
      final IdentifierQuotingStrategy quotingStrategy,
      final String quoteString) {

    requireNonNull(table, "No table provided");
    requireNonNull(quotingStrategy, "No identifier quoting strategy provided");

    final List<Column> columns = table.getColumns();
    return joinColumns(quotingStrategy, quoteString, columns);
  }

  /**
   * Gets a comma-separated list of columns for an index.
   *
   * @param tableConstraint Table constraint
   * @param quotingStrategy Identifier quoting strategy
   * @param quoteString
   * @return Comma-separated list of columns
   */
  public static String getColumnsListAsString(
      final TableConstraint tableConstraint,
      final IdentifierQuotingStrategy quotingStrategy,
      final String quoteString) {

    requireNonNull(tableConstraint, "No table constraint provided");
    requireNonNull(quotingStrategy, "No identifier quoting strategy provided");

    final List<TableConstraintColumn> columns = tableConstraint.getConstrainedColumns();
    return joinColumns(quotingStrategy, quoteString, columns);
  }

  /**
   * Gets a comma-separated list of columns for a foreign key.
   *
   * @param fk Foreign key
   * @param quotingStrategy Identifier quoting strategy
   * @param quoteString
   * @return Comma-separated list of columns
   */
  public static String getColumnsListAsString(
      final TableReference fk,
      final TableRelationshipType relationshipType,
      final IdentifierQuotingStrategy quotingStrategy,
      final String quoteString) {

    requireNonNull(fk, "No foreign key provided");
    requireNonNull(quotingStrategy, "No identifier quoting strategy provided");
    if (relationshipType == null || relationshipType == TableRelationshipType.none) {
      return "";
    }

    final List<Column> columns = new ArrayList<>();
    for (final ColumnReference columnReference : fk.getColumnReferences()) {
      switch (relationshipType) {
        case parent:
          columns.add(columnReference.getPrimaryKeyColumn());
          break;
        case child:
          columns.add(columnReference.getForeignKeyColumn());
          break;
        default:
      }
    }
    return joinColumns(quotingStrategy, quoteString, columns);
  }

  public static boolean isForeignKeyUnique(final TableReference foreignKey) {
    if (foreignKey == null) {
      return false;
    }
    final ColumnReference columnRef0 = foreignKey.getColumnReferences().get(0);
    final Table fkTable = columnRef0.getForeignKeyColumn().getParent();
    final Collection<List<String>> uniqueIndexCoumnNames = uniqueIndexCoumnNames(fkTable);
    final List<String> foreignKeyColumnNames = foreignKeyColumnNames(foreignKey);
    return uniqueIndexCoumnNames.contains(foreignKeyColumnNames);
  }

  public static String joinColumns(
      final List<? extends Column> columns,
      final boolean omitLargeObjectColumns,
      final Identifiers identifiers) {

    requireNonNull(columns, "No columns provided");
    requireNonNull(identifiers, "No identifiers provided");

    final List<String> columnsList = new ArrayList<>();
    for (int i = 0; i < columns.size(); i++) {
      final Column column = columns.get(i);
      final JavaSqlTypeGroup javaSqlTypeGroup =
          column.getColumnDataType().getJavaSqlType().getJavaSqlTypeGroup();
      if (!(omitLargeObjectColumns
          && (javaSqlTypeGroup == JavaSqlTypeGroup.large_object
              || javaSqlTypeGroup == JavaSqlTypeGroup.object))) {
        columnsList.add(identifiers.quoteName(column.getName()));
      }
    }
    return String.join(", ", columnsList);
  }

  public static Collection<List<String>> uniqueIndexCoumnNames(final Table table) {
    return indexCoumnNames(table, true);
  }

  private static Collection<List<String>> indexCoumnNames(
      final Table table, final boolean includeUniqueOnly) {
    final List<List<String>> allIndexCoumns = new ArrayList<>();
    if (table instanceof PartialDatabaseObject) {
      return allIndexCoumns;
    }

    for (final Index index : table.getIndexes()) {
      if (includeUniqueOnly && !index.isUnique()) {
        continue;
      }

      final List<String> indexColumns = columnNames(index);
      allIndexCoumns.add(indexColumns);
    }
    return allIndexCoumns;
  }

  private static String joinColumns(
      final IdentifierQuotingStrategy quotingStrategy,
      final String quoteString,
      final List<? extends Column> columns) {
    final Identifiers identifiers =
        identifiers()
            .withIdentifierQuotingStrategy(quotingStrategy)
            .withIdentifierQuoteString(quoteString)
            .build();

    return joinColumns(columns, false, identifiers);
  }

  private MetaDataUtility() {
    // Prevent instantiation
  }
}

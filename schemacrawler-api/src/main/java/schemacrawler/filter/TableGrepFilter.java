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
package schemacrawler.filter;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;

import schemacrawler.SchemaCrawlerLogger;
import schemacrawler.inclusionrule.InclusionRule;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKey;
import schemacrawler.schema.PartialDatabaseObject;
import schemacrawler.schema.Table;
import schemacrawler.schema.Trigger;
import schemacrawler.schemacrawler.GrepOptions;
import us.fatehi.utility.string.StringFormat;

class TableGrepFilter implements Predicate<Table> {

  private static final SchemaCrawlerLogger LOGGER =
      SchemaCrawlerLogger.getLogger(TableGrepFilter.class.getName());

  private final InclusionRule grepTableInclusionRule;
  private final InclusionRule grepColumnInclusionRule;
  private final InclusionRule grepDefinitionInclusionRule;
  private final boolean invertMatch;
  private final boolean grepOnlyMatching;

  public TableGrepFilter(final GrepOptions options) {

    requireNonNull(options, "No grep options provided");

    invertMatch = options.isGrepInvertMatch();
    grepOnlyMatching = options.isGrepOnlyMatching();

    grepTableInclusionRule = options.getGrepTableInclusionRule().orElse(null);
    grepColumnInclusionRule = options.getGrepColumnInclusionRule().orElse(null);
    grepDefinitionInclusionRule = options.getGrepDefinitionInclusionRule().orElse(null);
  }

  /**
   * Special case for "grep" like functionality. Handle table if a table column inclusion rule is
   * found, and at least one column matches the rule.
   *
   * @param table Table to check
   * @return Whether the column should be included
   */
  @Override
  public boolean test(final Table table) {
    final boolean checkIncludeForTables = grepTableInclusionRule != null;
    final boolean checkIncludeForColumns = grepColumnInclusionRule != null;
    final boolean checkIncludeForDefinitions = grepDefinitionInclusionRule != null;

    if (!checkIncludeForTables && !checkIncludeForColumns && !checkIncludeForDefinitions) {
      return true;
    }

    boolean includeForTables = false;
    boolean includeForColumns = false;
    boolean includeForDefinitions = false;

    if (checkIncludeForTables) {
      if (grepTableInclusionRule.test(table.getFullName())) {
        includeForTables = true;
      }
    }

    final List<Column> columns = table.getColumns();
    // Check if info-level=minimum, and no columns were retrieved
    if (columns.isEmpty()) {
      includeForColumns = true;
      includeForDefinitions = true;
    }
    for (final Column column : columns) {
      if (checkIncludeForColumns) {
        if (grepColumnInclusionRule.test(column.getFullName())) {
          includeForColumns = true;
          break;
        }
      }
      if (checkIncludeForDefinitions) {
        if (grepDefinitionInclusionRule.test(column.getRemarks())) {
          includeForDefinitions = true;
          break;
        }
      }
    }
    // Additional include checks for definitions
    if (checkIncludeForDefinitions) {
      if (grepDefinitionInclusionRule.test(table.getRemarks())) {
        includeForDefinitions = true;
      }
      if (grepDefinitionInclusionRule.test(table.getDefinition())) {
        includeForDefinitions = true;
      }
      for (final Trigger trigger : table.getTriggers()) {
        if (grepDefinitionInclusionRule.test(trigger.getActionStatement())) {
          includeForDefinitions = true;
          break;
        }
      }
    }

    boolean include =
        checkIncludeForTables && includeForTables
            || checkIncludeForColumns && includeForColumns
            || checkIncludeForDefinitions && includeForDefinitions;
    if (invertMatch) {
      include = !include;
    }

    // This is not strictly filter logic. It has the side effect of marking tables that need to
    // remain in the catalog, but are not matched by strict grep - that is, with --only-matching.
    if (grepOnlyMatching) {
      if (!include) {
        markAsNotStrictMatch(table);
      } else {
        // For included tables, mark partial references as not strict
        excludePartialReferences(table);
      }
    }

    if (!include) {
      LOGGER.log(Level.FINE, new StringFormat("Excluding table <%s>", table));
    }
    return include;
  }

  private void excludePartialReferences(final Table table) {
    for (final ForeignKey foreignKey : table.getExportedForeignKeys()) {
      for (final ColumnReference fkColumnRef : foreignKey) {
        final Table referencedTable = fkColumnRef.getForeignKeyColumn().getParent();
        if (referencedTable instanceof PartialDatabaseObject) {
          markAsNotStrictMatch(referencedTable);
        }
      }
    }
  }

  private void markAsNotStrictMatch(final Table table) {
    table.setAttribute("schemacrawler.table.no_grep_match", true);
  }
}

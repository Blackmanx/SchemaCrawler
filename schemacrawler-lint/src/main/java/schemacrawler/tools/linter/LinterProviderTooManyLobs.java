/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2025, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.tools.linter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import static java.util.Objects.requireNonNull;
import schemacrawler.schema.Column;
import schemacrawler.schema.JavaSqlTypeGroup;
import schemacrawler.schema.Table;
import schemacrawler.tools.lint.BaseLinter;
import schemacrawler.tools.lint.BaseLinterProvider;
import schemacrawler.tools.lint.LintCollector;
import schemacrawler.tools.lint.LintSeverity;
import schemacrawler.tools.lint.Linter;
import schemacrawler.tools.options.Config;
import us.fatehi.utility.property.PropertyName;

public class LinterProviderTooManyLobs extends BaseLinterProvider {

  private static final long serialVersionUID = -7901644028908017034L;

  public LinterProviderTooManyLobs() {
    super(LinterTooManyLobs.class.getName());
  }

  @Override
  public Linter newLinter(final LintCollector lintCollector) {
    return new LinterTooManyLobs(getPropertyName(), lintCollector);
  }
}

class LinterTooManyLobs extends BaseLinter {

  private int maxLargeObjectsInTable;

  LinterTooManyLobs(final PropertyName propertyName, final LintCollector lintCollector) {
    super(propertyName, lintCollector);
    setSeverity(LintSeverity.low);

    maxLargeObjectsInTable = 1;
  }

  @Override
  public String getSummary() {
    return "too many binary objects";
  }

  @Override
  protected void configure(final Config config) {
    requireNonNull(config, "No configuration provided");

    maxLargeObjectsInTable = config.getIntegerValue("max-large-objects", 1);
  }

  @Override
  protected void lint(final Table table, final Connection connection) {
    requireNonNull(table, "No table provided");

    final ArrayList<Column> lobColumns = findLobColumns(getColumns(table));
    if (lobColumns.size() > maxLargeObjectsInTable) {
      addTableLint(table, getSummary(), lobColumns);
    }
  }

  private ArrayList<Column> findLobColumns(final List<Column> columns) {
    final ArrayList<Column> lobColumns = new ArrayList<>();
    for (final Column column : columns) {
      if (!column.isColumnDataTypeKnown()) {
        continue;
      }
      final JavaSqlTypeGroup javaSqlTypeGroup =
          column.getColumnDataType().getJavaSqlType().getJavaSqlTypeGroup();
      if (javaSqlTypeGroup == JavaSqlTypeGroup.large_object) {
        lobColumns.add(column);
      }
    }
    return lobColumns;
  }
}

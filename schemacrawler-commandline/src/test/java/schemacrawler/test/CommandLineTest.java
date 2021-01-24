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
import static schemacrawler.test.utility.CommandlineTestUtility.commandlineExecution;
import static schemacrawler.test.utility.DatabaseTestUtility.loadHsqldbConfig;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.test.utility.DatabaseConnectionInfo;
import schemacrawler.test.utility.TestContext;
import schemacrawler.test.utility.TestContextParameterResolver;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.tools.command.text.schema.options.TextOutputFormat;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
public class CommandLineTest {

  private static final String COMMAND_LINE_OUTPUT = "command_line_output/";

  private static void run(
      final TestContext testContext,
      final DatabaseConnectionInfo connectionInfo,
      final Map<String, String> argsMap,
      final Map<String, String> config,
      final String command)
      throws Exception {
    run(testContext, connectionInfo, argsMap, config, command, TextOutputFormat.text);
  }

  private static void run(
      final TestContext testContext,
      final DatabaseConnectionInfo connectionInfo,
      final Map<String, String> argsMap,
      final Map<String, String> config,
      final String command,
      final TextOutputFormat outputFormat)
      throws Exception {
    argsMap.put("-no-info", Boolean.TRUE.toString());
    argsMap.put("-schemas", ".*\\.(?!FOR_LINT).*");
    argsMap.put("-info-level", "maximum");

    final Map<String, Object> runConfig = new HashMap<>();
    final Map<String, String> informationSchema = loadHsqldbConfig();
    runConfig.putAll(informationSchema);
    if (config != null) {
      runConfig.putAll(config);
    }

    final String extension;
    switch (outputFormat) {
      case text:
        extension = ".txt";
        break;
      default:
        extension = "." + outputFormat.getFormat();
        break;
    }

    assertThat(
        outputOf(commandlineExecution(connectionInfo, command, argsMap, runConfig, outputFormat)),
        hasSameContentAs(
            classpathResource(COMMAND_LINE_OUTPUT + testContext.testMethodName() + extension)));
  }

  private static void run(
      final TestContext testContext,
      final DatabaseConnectionInfo connectionInfo,
      final Map<String, String> argsMap,
      final String command)
      throws Exception {
    run(testContext, connectionInfo, argsMap, null, command, TextOutputFormat.text);
  }

  @Test
  public void commandLineColorOverrides(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", ".*");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.color_map.9900CC", ".*\\..*PUBLISHER.*");
    config.put("schemacrawler.format.color_map.FFFF00", ".*\\.BOOKS");

    run(testContext, connectionInfo, args, config, "brief", TextOutputFormat.html);
  }

  @Test
  public void commandLineOverridesWithConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", ".*");
    args.put("-routines", ".*");
    args.put("-sequences", ".*");
    args.put("-synonyms", ".*");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.table.pattern.include", ".*");
    config.put("schemacrawler.table.pattern.exclude", ".*A.*");
    config.put("schemacrawler.routine.pattern.include", ".*");
    config.put("schemacrawler.routine.pattern.exclude", ".*A.*");
    config.put("schemacrawler.sequence.pattern.include", ".*");
    config.put("schemacrawler.sequence.pattern.exclude", "");
    config.put("schemacrawler.synonym.pattern.include", ".*");
    config.put("schemacrawler.synonym.pattern.exclude", "");

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineOverridesWithGrepConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-grep-columns", ".*BOOKS.ID");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.grep.column.pattern.include", ".*AUTHORS.ID");
    config.put("schemacrawler.grep.column.pattern.exclude", "");

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineOverridesWithSomePortableNames(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-portable-names", Boolean.FALSE.toString());

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.hide_primarykey_names", Boolean.TRUE.toString());
    config.put("schemacrawler.format.hide_foreignkey_names", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineOverridesWithTextShowOptionsConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-no-remarks", Boolean.FALSE.toString());
    args.put("-portable-names", Boolean.FALSE.toString());

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.no_remarks", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_unqualified_names", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_standard_column_type_names", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_ordinal_numbers", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineRoutinesWithColumnsSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", "");
    args.put("-routines", ".*");
    args.put("-sort-columns", Boolean.TRUE.toString());
    // Testing no tables, all routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineRoutinesWithoutColumnsSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", "");
    args.put("-routines", ".*");
    args.put("-sort-columns", Boolean.FALSE.toString());
    // Testing no tables, all routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineRoutinesWithoutSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", "");
    args.put("-routines", ".*");
    args.put("-sort-routines", Boolean.FALSE.toString());
    // Testing no tables, all routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineRoutinesWithSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-tables", "");
    args.put("-routines", ".*");
    args.put("-sort-routines", Boolean.TRUE.toString());
    // Testing no tables, all routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineTablesWithColumnsSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-routines", "");
    args.put("-sort-columns", Boolean.TRUE.toString());
    // Testing all tables, no routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, null, "brief");
  }

  @Test
  public void commandLineTablesWithoutColumnsSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-routines", "");
    args.put("-sort-columns", Boolean.FALSE.toString());
    // Testing all tables, no routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineTablesWithoutSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-routines", "");
    args.put("-sort-tables", Boolean.FALSE.toString());
    // Testing all tables, no routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineTablesWithSorting(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-routines", "");
    args.put("-sort-tables", Boolean.TRUE.toString());
    // Testing all tables, no routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, null, "brief");
  }

  @Test
  public void commandLineWithConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.show_unqualified_names", Boolean.TRUE.toString());
    config.put("schemacrawler.table.pattern.include", ".*");
    config.put("schemacrawler.table.pattern.exclude", ".*A.*");
    config.put("schemacrawler.routine.pattern.include", ".*");
    config.put("schemacrawler.routine.pattern.exclude", ".*A.*");
    config.put("schemacrawler.sequence.pattern.include", ".*");
    config.put("schemacrawler.sequence.pattern.exclude", "");
    config.put("schemacrawler.synonym.pattern.include", ".*");
    config.put("schemacrawler.synonym.pattern.exclude", "");

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithDefaults(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-portable-names", Boolean.TRUE.toString());
    // Testing all tables, routines
    // Testing no sequences, synonyms

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineWithGrepConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.grep.column.pattern.include", ".*AUTHORS.ID");
    config.put("schemacrawler.grep.column.pattern.exclude", "");

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithNonDefaults(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-portable-names", Boolean.TRUE.toString());
    args.put("-tables", "");
    args.put("-routines", ".*");
    args.put("-sequences", ".*");
    args.put("-synonyms", ".*");

    run(testContext, connectionInfo, args, "brief");
  }

  @Test
  public void commandLineWithQueryInConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final String command = "query1";

    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put(command, "SELECT * FROM BOOKS.Books");

    run(testContext, connectionInfo, args, config, command);
  }

  @Test
  public void commandLineWithQueryOverInConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final String command = "query2";

    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put(command, "SELECT ${columns} FROM ${table} ORDER BY ${columns}");

    run(testContext, connectionInfo, args, config, command);
  }

  @Test
  public void commandLineWithQuoteOptionsConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.identifier_quoting_strategy", "quote_all");

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithSomePortableNames1(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    // args.put("-portable-names", Boolean.TRUE.toString());

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.hide_primarykey_names", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithSomePortableNames2(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    // args.put("-portable-names", Boolean.TRUE.toString());

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.hide_foreignkey_names", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithSortConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.sort_alphabetically.tables", Boolean.TRUE.toString());
    config.put("schemacrawler.format.sort_alphabetically.table_columns", Boolean.TRUE.toString());
    config.put(
        "schemacrawler.format.sort_alphabetically.table_foreignkeys", Boolean.TRUE.toString());
    config.put("schemacrawler.format.sort_alphabetically.table_indexes", Boolean.TRUE.toString());
    config.put("schemacrawler.format.sort_alphabetically.routines", Boolean.TRUE.toString());
    config.put("schemacrawler.format.sort_alphabetically.routine_columns", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithTextShowOptionsConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.hide_remarks", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_unqualified_names", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_standard_column_type_names", Boolean.TRUE.toString());
    config.put("schemacrawler.format.show_ordinal_numbers", Boolean.TRUE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }
}

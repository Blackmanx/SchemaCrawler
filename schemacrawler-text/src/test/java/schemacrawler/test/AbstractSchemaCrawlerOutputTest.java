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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.hasSameContentAndTypeAs;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.TestUtility.clean;

import java.io.IOException;
import java.sql.Connection;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.inclusionrule.ExcludeAll;
import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.inclusionrule.RegularExpressionInclusionRule;
import schemacrawler.schemacrawler.IdentifierQuotingStrategy;
import schemacrawler.schemacrawler.InfoLevel;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.LoadOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.test.utility.TestUtility;
import schemacrawler.tools.command.text.schema.options.SchemaTextDetailType;
import schemacrawler.tools.command.text.schema.options.SchemaTextOptions;
import schemacrawler.tools.command.text.schema.options.SchemaTextOptionsBuilder;
import schemacrawler.tools.command.text.schema.options.TextOutputFormat;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputFormat;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@TestInstance(PER_CLASS)
public abstract class AbstractSchemaCrawlerOutputTest {

  private static final String ORDINAL_OUTPUT = "ordinal_output/";
  private static final String TABLE_ROW_COUNT_OUTPUT = "table_row_count_output/";
  private static final String SHOW_WEAK_ASSOCIATIONS_OUTPUT = "show_weak_associations_output/";
  private static final String HIDE_CONSTRAINT_NAMES_OUTPUT = "hide_constraint_names_output/";
  private static final String UNQUALIFIED_NAMES_OUTPUT = "unqualified_names_output/";
  private static final String ROUTINES_OUTPUT = "routines_output/";
  private static final String NO_REMARKS_OUTPUT = "no_remarks_output/";
  private static final String WITH_TITLE_OUTPUT = "with_title_output/";
  private static final String NO_SCHEMA_COLORS_OUTPUT = "no_schema_colors_output/";
  private static final String IDENTIFIER_QUOTING_OUTPUT = "identifier_quoting_output/";

  private SchemaRetrievalOptions schemaRetrievalOptions;

  @Test
  public void compareHideConstraintNamesOutput(final Connection connection) throws Exception {
    clean(HIDE_CONSTRAINT_NAMES_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noSchemaCrawlerInfo(false)
        .showDatabaseInfo(true)
        .showJdbcDriverInfo(true)
        .noPrimaryKeyNames()
        .noForeignKeyNames()
        .noWeakAssociationNames()
        .noIndexNames()
        .noConstraintNames();
    textOptionsBuilder.noConstraintNames();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile =
                          "hidden_constraint_names." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeAllSequences()
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true);

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
                      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(HIDE_CONSTRAINT_NAMES_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @Test
  public void compareIdentifierQuotingOutput(final Connection connection) throws Exception {
    clean(IDENTIFIER_QUOTING_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noRemarks()
        .noSchemaCrawlerInfo()
        .showDatabaseInfo(false)
        .showJdbcDriverInfo(false);

    assertAll(
        Arrays.stream(IdentifierQuotingStrategy.values())
            .map(
                identifierQuotingStrategy ->
                    () -> {
                      final OutputFormat outputFormat = TextOutputFormat.text;
                      textOptionsBuilder.withIdentifierQuotingStrategy(identifierQuotingStrategy);
                      final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

                      final String referenceFile =
                          "schema_"
                              + identifierQuotingStrategy.name()
                              + "."
                              + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
                              .includeAllRoutines();
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions());

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(
                          SchemaTextOptionsBuilder.builder(textOptions).toConfig());

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(IDENTIFIER_QUOTING_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @Test
  public void compareNoRemarksOutput(final Connection connection) throws Exception {
    clean(NO_REMARKS_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noRemarks()
        .noSchemaCrawlerInfo()
        .showDatabaseInfo(false)
        .showJdbcDriverInfo(false);
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile = "schema_detailed." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.detailed());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(
                          SchemaTextOptionsBuilder.builder(textOptions).toConfig());

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(NO_REMARKS_OUTPUT + referenceFile), outputFormat));
                    }));
  }

  @Test
  public void compareNoSchemaColorsOutput(final Connection connection) throws Exception {
    clean(NO_SCHEMA_COLORS_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noRemarks()
        .noSchemaCrawlerInfo()
        .showDatabaseInfo(false)
        .showJdbcDriverInfo(false)
        .noSchemaColors();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile = "schema_detailed." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(new RegularExpressionInclusionRule(".*\\.BOOKS"))
                              .includeAllRoutines();
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions());

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(
                          SchemaTextOptionsBuilder.builder(textOptions).toConfig());

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(NO_SCHEMA_COLORS_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @Test
  public void compareOrdinalOutput(final Connection connection) throws Exception {
    clean(ORDINAL_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo().showJdbcDriverInfo();
    textOptionsBuilder.showOrdinalNumbers();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile =
                          "show_ordinal_numbers." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeAllSequences()
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true);

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
                      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(ORDINAL_OUTPUT + referenceFile), outputFormat));
                    }));
  }

  @Test
  public void compareRoutinesOutput(final Connection connection) throws Exception {
    clean(ROUTINES_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noSchemaCrawlerInfo(false)
        .showDatabaseInfo()
        .showJdbcDriverInfo()
        .showUnqualifiedNames();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile = "routines." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeTables(new ExcludeAll())
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true).noInfo();

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.details.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
                      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(ROUTINES_OUTPUT + referenceFile), outputFormat));
                    }));
  }

  @Test
  public void compareShowWeakAssociationsOutput(final Connection connection) throws Exception {
    clean(SHOW_WEAK_ASSOCIATIONS_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo().showJdbcDriverInfo();
    textOptionsBuilder.weakAssociations().noWeakAssociationNames();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile = "schema_standard." + outputFormat.getFormat();

                      final SchemaInfoLevelBuilder schemaInfoLevelBuilder =
                          SchemaInfoLevelBuilder.builder().withInfoLevel(InfoLevel.standard);

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(schemaInfoLevelBuilder.toOptions());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true);

                      final Config config = schemaTextOptionsBuilder.toConfig();
                      config.put("weak-associations", Boolean.TRUE);

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(config);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(SHOW_WEAK_ASSOCIATIONS_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @Test
  public void compareTableRowCountOutput(final Connection connection) throws Exception {
    clean(TABLE_ROW_COUNT_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder.noSchemaCrawlerInfo(false).showDatabaseInfo().showJdbcDriverInfo();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile = "schema_maximum." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true);

                      final Config additionalConfig = new Config();
                      additionalConfig.put("load-row-counts", true);
                      additionalConfig.merge(schemaTextOptionsBuilder.toConfig());

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(additionalConfig);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(TABLE_ROW_COUNT_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @Test
  public void compareUnqualifiedNamesOutput(final Connection connection) throws Exception {
    clean(UNQUALIFIED_NAMES_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noSchemaCrawlerInfo(false)
        .showDatabaseInfo()
        .showJdbcDriverInfo()
        .showUnqualifiedNames();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        outputFormats()
            .map(
                outputFormat ->
                    () -> {
                      final String referenceFile =
                          "show_unqualified_names." + outputFormat.getFormat();

                      final LimitOptionsBuilder limitOptionsBuilder =
                          LimitOptionsBuilder.builder()
                              .includeSchemas(
                                  new RegularExpressionExclusionRule(
                                      ".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"))
                              .includeAllSequences()
                              .includeAllRoutines();
                      final LoadOptionsBuilder loadOptionsBuilder =
                          LoadOptionsBuilder.builder()
                              .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum());
                      final SchemaCrawlerOptions schemaCrawlerOptions =
                          SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                              .withLimitOptions(limitOptionsBuilder.toOptions())
                              .withLoadOptions(loadOptionsBuilder.toOptions());

                      final SchemaTextOptionsBuilder schemaTextOptionsBuilder =
                          SchemaTextOptionsBuilder.builder(textOptions);
                      schemaTextOptionsBuilder.sortTables(true);

                      final SchemaCrawlerExecutable executable =
                          new SchemaCrawlerExecutable(SchemaTextDetailType.schema.name());
                      executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                      executable.setAdditionalConfiguration(schemaTextOptionsBuilder.toConfig());
                      executable.setSchemaRetrievalOptions(schemaRetrievalOptions);

                      assertThat(
                          outputOf(executableExecution(connection, executable, outputFormat)),
                          hasSameContentAndTypeAs(
                              classpathResource(UNQUALIFIED_NAMES_OUTPUT + referenceFile),
                              outputFormat));
                    }));
  }

  @BeforeAll
  public void schemaRetrievalOptions() throws IOException {
    schemaRetrievalOptions = TestUtility.newSchemaRetrievalOptions();
  }

  @Test
  public void titleOutput(final Connection connection) throws Exception {
    clean(WITH_TITLE_OUTPUT);

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder.builder();
    textOptionsBuilder
        .noRemarks()
        .noSchemaCrawlerInfo()
        .showDatabaseInfo(false)
        .showJdbcDriverInfo(false);
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    assertAll(
        Arrays.asList("list", "schema")
            .stream()
            .flatMap(
                command ->
                    outputFormats()
                        .map(
                            outputFormat ->
                                () -> {
                                  final String referenceFile =
                                      String.format(
                                          "%s_with_title.%s", command, outputFormat.getFormat());

                                  final LimitOptionsBuilder limitOptionsBuilder =
                                      LimitOptionsBuilder.builder()
                                          .includeSchemas(
                                              new RegularExpressionInclusionRule(".*\\.BOOKS"));
                                  final SchemaCrawlerOptions schemaCrawlerOptions =
                                      SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
                                          .withLimitOptions(limitOptionsBuilder.toOptions());

                                  final OutputOptionsBuilder outputOptionsBuilder =
                                      OutputOptionsBuilder.builder()
                                          .title("Database Design for Books and Publishers");
                                  final OutputOptions outputOptions =
                                      outputOptionsBuilder.toOptions();

                                  final SchemaCrawlerExecutable executable =
                                      new SchemaCrawlerExecutable(command);
                                  executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
                                  executable.setAdditionalConfiguration(
                                      SchemaTextOptionsBuilder.builder(textOptions).toConfig());
                                  executable.setOutputOptions(outputOptions);

                                  assertThat(
                                      outputOf(
                                          executableExecution(
                                              connection, executable, outputFormat)),
                                      hasSameContentAndTypeAs(
                                          classpathResource(WITH_TITLE_OUTPUT + referenceFile),
                                          outputFormat));
                                })));
  }

  protected abstract Stream<OutputFormat> outputFormats();
}

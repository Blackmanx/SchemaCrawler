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

package schemacrawler.integration.test;

import static java.nio.file.Files.createDirectories;
import static org.apache.commons.io.FileUtils.deleteDirectory;
import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.schemacrawler.DatabaseObjectRuleForInclusion.ruleForSchemaInclusion;
import static schemacrawler.test.utility.DatabaseTestUtility.schemaRetrievalOptionsDefault;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.hasSameContentAndTypeAs;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.tools.command.text.diagram.options.DiagramOptionsBuilder.builder;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import schemacrawler.inclusionrule.RegularExpressionExclusionRule;
import schemacrawler.schemacrawler.FilterOptions;
import schemacrawler.schemacrawler.FilterOptionsBuilder;
import schemacrawler.schemacrawler.GrepOptions;
import schemacrawler.schemacrawler.GrepOptionsBuilder;
import schemacrawler.schemacrawler.LimitOptionsBuilder;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.test.utility.ResolveTestContext;
import schemacrawler.test.utility.TestContext;
import schemacrawler.test.utility.TestUtility;
import schemacrawler.tools.command.text.diagram.options.DiagramOptions;
import schemacrawler.tools.command.text.diagram.options.DiagramOptionsBuilder;
import schemacrawler.tools.command.text.diagram.options.DiagramOutputFormat;
import schemacrawler.tools.command.text.schema.options.SchemaTextDetailType;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;

@ResolveTestContext
public class DiagramRendererOptionsAdditionalSchemasTest {

  private static final String ADDITIONAL_DIAGRAM_OPTIONS_OUTPUT =
      "additional_diagram_options_output/";
  private static Path directory;

  @BeforeAll
  public static void clean() throws Exception {
    TestUtility.clean(ADDITIONAL_DIAGRAM_OPTIONS_OUTPUT);
  }

  @BeforeAll
  public static void setupDirectory(final TestContext testContext) throws Exception {
    directory =
        testContext.resolveTargetFromRootPath(
            "test-output-diagrams/"
                + DiagramRendererOptionsAdditionalSchemasTest.class.getSimpleName());
    deleteDirectory(directory.toFile());
    createDirectories(directory);
  }

  private static void executableDiagram(
      final String command,
      final Connection connection,
      final SchemaCrawlerOptions options,
      final Config config,
      final DiagramOptions diagramOptions,
      final String testMethodName)
      throws Exception {

    SchemaCrawlerOptions schemaCrawlerOptions = options;
    if (options.getLimitOptions().isIncludeAll(ruleForSchemaInclusion)) {
      final LimitOptionsBuilder limitOptionsBuilder =
          LimitOptionsBuilder.builder()
              .fromOptions(options.getLimitOptions())
              .includeSchemas(new RegularExpressionExclusionRule(".*\\.SYSTEM_LOBS|.*\\.FOR_LINT"));
      schemaCrawlerOptions = options.withLimitOptions(limitOptionsBuilder.toOptions());
    }

    final DiagramOptionsBuilder diagramOptionsBuilder = builder(diagramOptions);
    diagramOptionsBuilder.sortTables(true);
    diagramOptionsBuilder.noInfo(diagramOptions.isNoInfo());

    final Config additionalConfig = new Config();
    additionalConfig.merge(config);
    additionalConfig.merge(diagramOptionsBuilder.toConfig());
    additionalConfig.put("schemacrawler.format.hide_weakassociation_names", "true");

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(command);
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setAdditionalConfiguration(additionalConfig);
    executable.setConnection(connection);
    executable.setSchemaRetrievalOptions(schemaRetrievalOptionsDefault);

    // Generate diagram, so that we have something to look at, even if
    // the DOT file comparison fails
    saveDiagram(executable, testMethodName);

    // Check DOT file
    final String referenceFileName = testMethodName;
    assertThat(
        outputOf(executableExecution(connection, executable, DiagramOutputFormat.scdot)),
        hasSameContentAndTypeAs(
            classpathResource(ADDITIONAL_DIAGRAM_OPTIONS_OUTPUT + referenceFileName + ".dot"),
            DiagramOutputFormat.scdot));
  }

  private static void saveDiagram(
      final SchemaCrawlerExecutable executable, final String testMethodName) throws Exception {
    final Path testDiagramFile = directory.resolve(testMethodName + ".png");

    final OutputOptions oldOutputOptions = executable.getOutputOptions();
    final OutputOptions outputOptions =
        OutputOptionsBuilder.builder()
            .fromOptions(oldOutputOptions)
            .withOutputFile(testDiagramFile)
            .withOutputFormat(DiagramOutputFormat.png)
            .toOptions();

    executable.setOutputOptions(outputOptions);

    executable.execute();
  }

  @Test
  @DisplayName("Diagram with no hanging foreign keys")
  public void executableAdditionalForDiagram_01(final TestContext testContext) throws Exception {

    final Connection connection = getConnection();
    final DiagramOptionsBuilder diagramOptionsBuilder =
        builder().showForeignKeyCardinality().showPrimaryKeyCardinality();
    final DiagramOptions diagramOptions = diagramOptionsBuilder.toOptions();

    final GrepOptions grepOptions =
        GrepOptionsBuilder.builder()
            .includeGreppedTables(tableName -> tableName.endsWith("TABLE3"))
            .toOptions();

    final FilterOptions filterOptions =
        FilterOptionsBuilder.builder()
            .parentTableFilterDepth(1)
            .childTableFilterDepth(1)
            .toOptions();

    final SchemaCrawlerOptions options =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions()
            .withGrepOptions(grepOptions)
            .withFilterOptions(filterOptions);

    executableDiagram(
        SchemaTextDetailType.schema.name(),
        connection,
        options,
        new Config(),
        diagramOptions,
        testContext.testMethodName());
  }

  private Connection getConnection() throws SQLException {
    final EmbeddedDatabase db =
        new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScript("table_chain.sql")
            .build();

    final Connection connection = db.getConnection();
    return connection;
  }
}

/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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
package schemacrawler.test.sitegen;

import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.exists;
import static java.nio.file.Files.move;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.CommandlineTestUtility.commandlineExecution;
import static schemacrawler.test.utility.DatabaseTestUtility.loadHsqldbConfig;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.test.utility.DatabaseConnectionInfo;
import schemacrawler.test.utility.TestAssertNoSystemErrOutput;
import schemacrawler.test.utility.TestAssertNoSystemOutOutput;
import schemacrawler.test.utility.TestContext;
import schemacrawler.test.utility.TestContextParameterResolver;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.tools.integration.diagram.DiagramOutputFormat;

@ExtendWith(TestAssertNoSystemErrOutput.class)
@ExtendWith(TestAssertNoSystemOutOutput.class)
@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
@EnabledIfSystemProperty(named = "distrib", matches = "^((?!(false|no)).)*$")
public class SiteDiagramVariationsTest {

  private static void run(
      final DatabaseConnectionInfo connectionInfo,
      final Map<String, String> argsMap,
      final Map<String, String> config,
      final Path outputFile)
      throws Exception {
    deleteIfExists(outputFile);
    assertThat(exists(outputFile), is(false));

    final Map<String, Object> runConfig = new HashMap<>();
    final Map<String, String> informationSchema = loadHsqldbConfig();
    runConfig.putAll(informationSchema);
    if (config != null) {
      runConfig.putAll(config);
    }

    final Path pngFile =
        commandlineExecution(connectionInfo, "schema", argsMap, runConfig, DiagramOutputFormat.png);
    move(pngFile, outputFile, REPLACE_EXISTING);
  }

  private Path directory;

  @BeforeEach
  public void _setupDirectory(final TestContext testContext)
      throws IOException, URISyntaxException {
    if (directory != null) {
      return;
    }
    directory = testContext.resolveTargetFromRootPath("_website/diagram-examples");
  }

  @Test
  public void diagram(final TestContext testContext, final DatabaseConnectionInfo connectionInfo)
      throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "maximum");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_10_no_schema_colors(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-portable-names", "true");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.no_schema_colors", "true");

    run(connectionInfo, args, config, diagramPath(testContext));
  }

  @Test
  public void diagram_11_title(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-title", "Books and Publishers Schema");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_12_graphviz_attributes(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-portable-names", "true");

    final Map<String, String> config = new HashMap<>();
    final String GRAPH = "schemacrawler.graph.graphviz.graph.";
    config.put(GRAPH + "rankdir", "RL");
    config.put(GRAPH + "fontname", "Helvetica");

    final String NODE = "schemacrawler.graph.graphviz.node.";
    config.put(NODE + "fontname", "Helvetica");
    config.put(NODE + "shape", "none");

    final String EDGE = "schemacrawler.graph.graphviz.edge.";
    config.put(EDGE + "fontname", "Helvetica");

    // Test
    config.put("schemacrawler.graph.graphviz.graph.splines", "ortho");

    run(connectionInfo, args, config, diagramPath(testContext));
  }

  @Test
  public void diagram_2_portablenames(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "maximum");
    args.put("-portable-names", "true");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_3_important_columns(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("c", "brief");
    args.put("-portable-names", "true");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_4_ordinals(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-portable-names", "true");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.format.show_ordinal_numbers", "true");

    run(connectionInfo, args, config, diagramPath(testContext));
  }

  @Test
  public void diagram_5_alphabetical(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-portable-names", "true");
    args.put("-sort-columns", "true");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_6_grep(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "maximum");
    args.put("-portable-names", "true");
    args.put("-grep-columns", ".*\\.BOOKS\\..*\\.ID");
    args.put("-table-types", "TABLE");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_7_grep_onlymatching(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "maximum");
    args.put("-portable-names", "true");
    args.put("-grep-columns", ".*\\.BOOKS\\..*\\.ID");
    args.put("-only-matching", "true");
    args.put("-table-types", "TABLE");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  @Test
  public void diagram_8_no_cardinality(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "standard");
    args.put("-portable-names", "true");

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.graph.show.primarykey.cardinality", "false");
    config.put("schemacrawler.graph.show.foreignkey.cardinality", "false");

    run(connectionInfo, args, config, diagramPath(testContext));
  }

  @Test
  public void diagram_9_row_counts(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();
    args.put("-info-level", "maximum");
    args.put("-load-row-counts", "true");
    args.put("-portable-names", "true");

    run(connectionInfo, args, null, diagramPath(testContext));
  }

  private Path diagramPath(final TestContext testContext) {
    return directory.resolve(testContext.testMethodName() + ".png");
  }
}

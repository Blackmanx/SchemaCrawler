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
import schemacrawler.tools.command.text.diagram.options.DiagramOutputFormat;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
@ExtendWith(TestContextParameterResolver.class)
public class CommandLineDiagramTest {

  private static final String COMMAND_LINE_DIAGRAM_OUTPUT = "command_line_diagram_output/";

  private static void run(
      final TestContext testContext,
      final DatabaseConnectionInfo connectionInfo,
      final Map<String, String> argsMap,
      final Map<String, String> config,
      final String command)
      throws Exception {
    argsMap.put("--no-info", Boolean.TRUE.toString());
    argsMap.put("--schemas", ".*\\.(?!FOR_LINT).*");
    argsMap.put("--info-level", "maximum");

    final Map<String, Object> runConfig = new HashMap<>();
    final Map<String, String> informationSchema = loadHsqldbConfig();
    runConfig.putAll(informationSchema);
    if (config != null) {
      runConfig.putAll(config);
    }

    assertThat(
        outputOf(
            commandlineExecution(
                connectionInfo, command, argsMap, runConfig, DiagramOutputFormat.scdot)),
        hasSameContentAs(
            classpathResource(
                COMMAND_LINE_DIAGRAM_OUTPUT + testContext.testMethodName() + ".scdot")));
  }

  @Test
  public void commandLineWithCardinalityOptionsConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.graph.show.primarykey.cardinality", Boolean.FALSE.toString());
    config.put("schemacrawler.graph.show.foreignkey.cardinality", Boolean.FALSE.toString());

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithDefaults(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();

    run(testContext, connectionInfo, args, config, "brief");
  }

  @Test
  public void commandLineWithGraphvizAttributesConfig(
      final TestContext testContext, final DatabaseConnectionInfo connectionInfo) throws Exception {
    final Map<String, String> args = new HashMap<>();

    final Map<String, String> config = new HashMap<>();
    config.put("schemacrawler.graph.graphviz.graph.ranksep", String.valueOf(3));

    run(testContext, connectionInfo, args, config, "brief");
  }
}

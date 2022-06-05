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

package schemacrawler.test.script;

import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.ScriptTestUtility.scriptExecution;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import schemacrawler.test.utility.AssertNoSystemOutOutput;
import schemacrawler.test.utility.ResolveTestContext;
import schemacrawler.test.utility.TestContext;
import schemacrawler.test.utility.WithSystemProperty;
import schemacrawler.test.utility.WithTestDatabase;

@AssertNoSystemOutOutput
@ResolveTestContext
@WithTestDatabase
public class DiagramScriptTest {

  @Test
  @WithSystemProperty(key = "python.console.encoding", value = "UTF-8")
  public void dbml(final TestContext testContext, final Connection connection) throws Exception {
    assertThat(
        outputOf(scriptExecution(connection, "/dbml.py")),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }

  @Test
  @WithSystemProperty(key = "python.console.encoding", value = "UTF-8")
  public void mermaid(final TestContext testContext, final Connection connection) throws Exception {
    assertThat(
        outputOf(scriptExecution(connection, "/mermaid.py")),
        hasSameContentAs(classpathResource(testContext.testMethodFullName() + ".txt")));
  }
}

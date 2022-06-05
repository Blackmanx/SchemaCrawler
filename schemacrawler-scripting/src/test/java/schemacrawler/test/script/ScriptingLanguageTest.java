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
import static schemacrawler.test.utility.DatabaseTestUtility.schemaRetrievalOptionsDefault;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.executableOf;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.TestUtility.copyResourceToTempFile;

import java.nio.file.Path;
import java.sql.Connection;

import org.junit.jupiter.api.Test;

import schemacrawler.test.utility.AssertNoSystemOutOutput;
import schemacrawler.test.utility.WithTestDatabase;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;

@AssertNoSystemOutOutput
@WithTestDatabase
public class ScriptingLanguageTest {

  private static Path executableScriptFromFile(
      final Connection connection, final String language, final Path scriptFile) throws Exception {

    final Config additionalConfig = new Config();
    additionalConfig.put("script", scriptFile.toString());
    additionalConfig.put("script-language", language);

    final SchemaCrawlerExecutable executable = executableOf("script");
    executable.setAdditionalConfiguration(additionalConfig);
    executable.setSchemaRetrievalOptions(schemaRetrievalOptionsDefault);

    return executableExecution(connection, executable, "text");
  }

  @Test
  public void executableGroovy(final Connection connection) throws Exception {
    final Path scriptFile = copyResourceToTempFile("/plaintextschema.groovy");
    assertThat(
        outputOf(executableScriptFromFile(connection, "groovy", scriptFile)),
        hasSameContentAs(classpathResource("script_output.txt")));
  }
}

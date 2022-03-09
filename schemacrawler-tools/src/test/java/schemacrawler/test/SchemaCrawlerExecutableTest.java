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

import static java.lang.System.lineSeparator;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static us.fatehi.utility.IOUtility.createTempFilePath;
import static us.fatehi.utility.IOUtility.readFully;

import java.io.FileReader;
import java.nio.file.Path;
import java.sql.Connection;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schema.Catalog;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaCrawlerOptionsBuilder;
import schemacrawler.schemacrawler.SchemaRetrievalOptions;
import schemacrawler.schemacrawler.SchemaRetrievalOptionsBuilder;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.test.utility.ExecutableTestUtility;
import schemacrawler.test.utility.TestDatabaseConnectionParameterResolver;
import schemacrawler.test.utility.WithSystemProperty;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.options.Config;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.OutputOptionsBuilder;

@ExtendWith(TestDatabaseConnectionParameterResolver.class)
public class SchemaCrawlerExecutableTest {

  @Test
  public void executable(final Connection connection) throws Exception {

    final Path testOutputFile = createTempFilePath("sc", "data");

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("test-command");

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();

    final OutputOptions outputOptions =
        ExecutableTestUtility.newOutputOptions("text", testOutputFile);

    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    executable.setOutputOptions(outputOptions);
    executable.setConnection(connection);
    executable.execute();

    assertThat(
        "Output generated from schemacrawler.test.utility.testcommand.TestCommand"
            + lineSeparator()
            + "TestOptions [testCommandParameter=]"
            + lineSeparator(),
        equalTo(readFully(new FileReader(testOutputFile.toFile()))));
    assertThat(executable.toString(), is("test-command"));

    assertThat(executable.getOutputOptions(), is(outputOptions));
    assertThat(executable.getSchemaCrawlerOptions(), is(schemaCrawlerOptions));

    final Catalog catalog = executable.getCatalog();
    assertThat(catalog.getSchemas(), hasSize(6));
    assertThat(catalog.getTables(), hasSize(19));
  }

  @Test
  @WithSystemProperty(key = "SC_WITHOUT_DATABASE_PLUGIN", value = "hsqldb")
  public void executable_bad_command(final Connection connection) throws Exception {

    final String command1 = "bad-command";
    final SchemaCrawlerExecutable executable1 = new SchemaCrawlerExecutable(command1);
    executable1.setConnection(connection);
    final InternalRuntimeException ex1 =
        assertThrows(InternalRuntimeException.class, () -> executable1.execute());
    assertThat(ex1.getMessage(), is("Unknown command <" + command1 + ">"));

    final String command2 = "test-command";
    final SchemaCrawlerExecutable executable2 = new SchemaCrawlerExecutable(command2);
    executable2.setConnection(connection);
    final Config config = new Config();
    config.put("return-null", "true");
    executable2.setAdditionalConfiguration(config);
    final InternalRuntimeException ex2 =
        assertThrows(InternalRuntimeException.class, () -> executable2.execute());
    assertThat(ex2.getMessage(), is("Cannot run command <" + command2 + ">"));
  }

  @Test
  public void executable_options(final Connection connection) throws Exception {

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("test-command");

    // SchemaCrawler options
    assertThat(executable.getSchemaCrawlerOptions(), is(not(nullValue())));

    final SchemaCrawlerOptions schemaCrawlerOptions =
        SchemaCrawlerOptionsBuilder.newSchemaCrawlerOptions();
    executable.setSchemaCrawlerOptions(schemaCrawlerOptions);
    assertThat(executable.getSchemaCrawlerOptions(), is(sameInstance(schemaCrawlerOptions)));

    executable.setSchemaCrawlerOptions(null);
    assertThat(executable.getSchemaCrawlerOptions(), is(not(nullValue())));
    assertThat(executable.getSchemaCrawlerOptions(), is(not(sameInstance(schemaCrawlerOptions))));

    // // Output options
    assertThat(executable.getOutputOptions(), is(not(nullValue())));

    final OutputOptions outputOptions = OutputOptionsBuilder.newOutputOptions();
    executable.setOutputOptions(outputOptions);
    assertThat(executable.getOutputOptions(), is(sameInstance(outputOptions)));

    executable.setOutputOptions(null);
    assertThat(executable.getOutputOptions(), is(not(nullValue())));
    assertThat(executable.getOutputOptions(), is(not(sameInstance(outputOptions))));

    // Schema retrieval options
    assertThat(executable.getSchemaRetrievalOptions(), is(nullValue()));

    final SchemaRetrievalOptions schemaRetrievalOptions =
        SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    executable.setSchemaRetrievalOptions(schemaRetrievalOptions);
    assertThat(executable.getSchemaRetrievalOptions(), is(sameInstance(schemaRetrievalOptions)));

    executable.setSchemaRetrievalOptions(null);
    assertThat(executable.getSchemaRetrievalOptions(), is(nullValue()));
    assertThat(
        executable.getSchemaRetrievalOptions(), is(not(sameInstance(schemaRetrievalOptions))));

    // Catalog
    assertThat(executable.getCatalog(), is(nullValue()));

    final Catalog catalog = mock(Catalog.class);
    executable.setCatalog(catalog);
    assertThat(executable.getCatalog(), is(sameInstance(catalog)));

    executable.setCatalog(null);
    assertThat(executable.getCatalog(), is(nullValue()));
    assertThat(executable.getCatalog(), is(not(sameInstance(catalog))));
  }

  @Test
  public void executable_with_settings(final Connection connection) throws Exception {

    final Path testOutputFile = createTempFilePath("sc", "data");
    final Catalog mockCatalog = mock(Catalog.class);
    final SchemaRetrievalOptions mockSchemaRetrievalOptions =
        SchemaRetrievalOptionsBuilder.newSchemaRetrievalOptions();
    final Config config = new Config();
    config.put("uses-connection", "false");

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable("test-command");

    final OutputOptions outputOptions =
        ExecutableTestUtility.newOutputOptions("text", testOutputFile);

    executable.setOutputOptions(outputOptions);
    executable.setConnection(connection);
    executable.setAdditionalConfiguration(config);
    executable.setCatalog(mockCatalog);
    executable.setSchemaRetrievalOptions(mockSchemaRetrievalOptions);
    executable.execute();

    assertThat(
        "Output generated from schemacrawler.test.utility.testcommand.TestCommand"
            + lineSeparator()
            + "TestOptions [testCommandParameter=]"
            + lineSeparator(),
        equalTo(readFully(new FileReader(testOutputFile.toFile()))));
    assertThat(executable.toString(), is("test-command"));

    assertThat(executable.getCatalog(), is(sameInstance(mockCatalog)));
    assertThat(
        executable.getSchemaRetrievalOptions(), is(sameInstance(mockSchemaRetrievalOptions)));
  }
}

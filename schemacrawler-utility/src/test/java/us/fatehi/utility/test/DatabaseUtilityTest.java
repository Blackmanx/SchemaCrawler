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

package us.fatehi.utility.test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;

import us.fatehi.utility.DatabaseUtility;

@TestInstance(Lifecycle.PER_CLASS)
public class DatabaseUtilityTest {

  private Connection connection;

  @Test
  public void checkConnection() throws SQLException {

    assertThat(DatabaseUtility.checkConnection(connection), is(connection));

    final Connection mockConnection = mock(Connection.class);
    when(mockConnection.isClosed()).thenReturn(true);

    final SQLException exception1 =
        assertThrows(
            SQLException.class,
            () -> assertThat(DatabaseUtility.checkConnection(null), is(nullValue())));
    assertThat(exception1.getMessage(), is("No database connection provided"));

    final SQLException exception2 =
        assertThrows(
            SQLException.class,
            () -> assertThat(DatabaseUtility.checkConnection(mockConnection), is(nullValue())));
    assertThat(exception2.getMessage(), is("Connection is closed"));
  }

  @Test
  public void checkResultSet() throws SQLException {

    final ResultSet results = mock(ResultSet.class);

    assertThat(DatabaseUtility.checkResultSet(results), is(results));

    when(results.isClosed()).thenReturn(true);

    final SQLException exception1 =
        assertThrows(
            SQLException.class,
            () -> assertThat(DatabaseUtility.checkResultSet(null), is(nullValue())));
    assertThat(exception1.getMessage(), is("No result-set provided"));

    final SQLException exception2 =
        assertThrows(
            SQLException.class,
            () -> assertThat(DatabaseUtility.checkResultSet(results), is(nullValue())));
    assertThat(exception2.getMessage(), is("Result-set is closed"));
  }

  @BeforeAll
  public void createDatabase() throws Exception {

    final EmbeddedDatabase db =
        new EmbeddedDatabaseBuilder()
            .generateUniqueName(true)
            .setScriptEncoding("UTF-8")
            .ignoreFailedDrops(true)
            .addScript("testdb.sql")
            .build();

    connection = db.getConnection();
  }

  @Test
  public void executeSql() throws SQLException {

    final Statement statement = connection.createStatement();

    assertThat(DatabaseUtility.executeSql(null, "<some query>"), is(nullValue()));
    assertThat(DatabaseUtility.executeSql(statement, null), is(nullValue()));

    assertThat(
        DatabaseUtility.executeSql(statement, "SELECT COL1 FROM TABLE1 WHERE ENTITY_ID = 1"),
        is(not(nullValue())));
    assertThat(
        DatabaseUtility.executeSql(statement, "UPDATE TABLE1 SET COL2 = 'GHI' WHERE ENTITY_ID = 1"),
        is(nullValue()));
  }

  @Test
  public void executeSql_throw() throws SQLException {

    final Statement statement = mock(Statement.class);
    doThrow(new SQLException("Exception using a mocked statement")).when(statement).execute(any());

    final SQLException exception =
        assertThrows(
            SQLException.class,
            () ->
                assertThat(DatabaseUtility.executeSql(statement, "<some query>"), is(nullValue())));
    assertThat(exception.getMessage(), is("Exception using a mocked statement"));
  }

  @Test
  public void executeSqlForLong() throws SQLException {

    // Happy path
    assertThat(
        DatabaseUtility.executeSqlForLong(
            connection, "SELECT COL3 FROM TABLE1 WHERE ENTITY_ID = 1"),
        is(2L));

    // Unhappy paths
    Exception exception;
    // NULL in database
    exception =
        assertThrows(
            SQLException.class,
            () ->
                DatabaseUtility.executeSqlForLong(
                    connection, "SELECT COL3 FROM TABLE1 WHERE ENTITY_ID = 2"));
    assertThat(exception.getMessage(), startsWith("Cannot get a long value"));
    // No rows of data
    exception =
        assertThrows(
            SQLException.class,
            () ->
                DatabaseUtility.executeSqlForLong(
                    connection, "SELECT COL3 FROM TABLE1 WHERE ENTITY_ID = 3"));
    assertThat(exception.getMessage(), startsWith("Cannot get a long value"));
    // Not a number
    exception =
        assertThrows(
            SQLException.class,
            () ->
                DatabaseUtility.executeSqlForLong(
                    connection, "SELECT COL1 FROM TABLE1 WHERE ENTITY_ID = 1"));
    assertThat(exception.getMessage(), startsWith("Cannot get a long value"));
  }

  @Test
  public void executeSqlForScalar() throws SQLException {

    // Happy path
    assertThat(
        DatabaseUtility.executeSqlForScalar(
            connection, "SELECT COL3 FROM TABLE1 WHERE COL1 = 'ABC'"),
        is(new BigDecimal(2)));
    // Happy path - NULL in database
    assertThat(
        DatabaseUtility.executeSqlForScalar(
            connection, "SELECT COL3 FROM TABLE1 WHERE COL1 = 'XYZ'"),
        is(nullValue()));
    // Happy path - no rows of data
    assertThat(
        DatabaseUtility.executeSqlForScalar(
            connection, "SELECT COL3 FROM TABLE1 WHERE COL1 = 'ZZZ'"),
        is(nullValue()));

    // Unhappy paths
    Exception exception;
    // Too many rows
    exception =
        assertThrows(
            SQLException.class,
            () -> DatabaseUtility.executeSqlForScalar(connection, "SELECT COL3 FROM TABLE1"));
    assertThat(exception.getMessage(), startsWith("Too many rows"));
    // Too many columns
    exception =
        assertThrows(
            SQLException.class,
            () -> DatabaseUtility.executeSqlForScalar(connection, "SELECT COL2, COL3 FROM TABLE1"));
    assertThat(exception.getMessage(), startsWith("Too many columns"));
  }
}

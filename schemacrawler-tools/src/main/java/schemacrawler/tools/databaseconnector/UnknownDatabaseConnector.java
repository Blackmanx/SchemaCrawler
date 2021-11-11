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
package schemacrawler.tools.databaseconnector;

import java.util.regex.Pattern;

import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.schemacrawler.SchemaCrawlerRuntimeException;
import us.fatehi.utility.PropertiesUtility;

final class UnknownDatabaseConnector extends DatabaseConnector {

  private static final Pattern[] patterns =
      new Pattern[] {
        Pattern.compile("jdbc:db2:.*"),
        Pattern.compile("jdbc:(mysql|mariadb):.*"),
        Pattern.compile("jdbc:oracle:.*"),
        Pattern.compile("jdbc:postgresql:.*"),
        Pattern.compile("jdbc:sqlite:.*"),
        Pattern.compile("jdbc:sqlserver:.*"),
      };

  /** Constructor for unknown databases. Bypass the null-checks of the main constructor */
  UnknownDatabaseConnector() {
    super(
        DatabaseServerType.UNKNOWN,
        url -> false,
        (informationSchemaViewsBuilder, connection) -> {},
        (schemaRetrievalOptionsBuilder, connection) -> {},
        limitOptionsBuilder -> {},
        () -> DatabaseConnectionUrlBuilder.builder(""));
  }

  @Override
  public DatabaseConnectionSource newDatabaseConnectionSource(
      final DatabaseConnectionOptions connectionOptions) {
    final DatabaseConnectionSource databaseConnectionSource =
        super.newDatabaseConnectionSource(connectionOptions);

    final String url = databaseConnectionSource.getConnectionUrl();
    failIfSchemaCrawlerPluginsNotAvailable(url);

    return databaseConnectionSource;
  }

  private void failIfSchemaCrawlerPluginsNotAvailable(final String url) {
    final String withoutDatabasePlugin =
        PropertiesUtility.getSystemConfigurationProperty(
            "SC_IGNORE_MISSING_DATABASE_PLUGIN", Boolean.FALSE.toString());
    if (!Boolean.valueOf(withoutDatabasePlugin)) {
      // Check if SchemaCrawler database plugin is in use
      for (final Pattern pattern : patterns) {
        if (pattern.matcher(url).matches()) {
          throw new SchemaCrawlerRuntimeException(
              String.format(
                  "SchemaCrawler database plugin should be on the CLASSPATH for <%s>", url));
        }
      }
    }
  }
}

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

package schemacrawler.tools.databaseconnector;

import static java.util.Comparator.naturalOrder;
import static us.fatehi.utility.Utility.isBlank;
import static us.fatehi.utility.database.DatabaseUtility.checkConnection;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

import schemacrawler.schemacrawler.DatabaseServerType;
import schemacrawler.schemacrawler.exceptions.InternalRuntimeException;
import schemacrawler.tools.executable.commandline.PluginCommand;
import us.fatehi.utility.string.StringFormat;

/**
 * Registry for mapping database connectors from DatabaseConnector-line switch.
 *
 * @author Sualeh Fatehi
 */
public final class DatabaseConnectorRegistry implements Iterable<DatabaseServerType> {

  private static final Logger LOGGER = Logger.getLogger(DatabaseConnectorRegistry.class.getName());

  private static DatabaseConnectorRegistry databaseConnectorRegistrySingleton;

  public static DatabaseConnectorRegistry getDatabaseConnectorRegistry() {
    if (databaseConnectorRegistrySingleton == null) {
      databaseConnectorRegistrySingleton = new DatabaseConnectorRegistry();
    }
    return databaseConnectorRegistrySingleton;
  }

  private static Map<String, DatabaseConnector> loadDatabaseConnectorRegistry() {

    final Map<String, DatabaseConnector> databaseConnectorRegistry = new HashMap<>();

    try {
      final ServiceLoader<DatabaseConnector> serviceLoader =
          ServiceLoader.load(
              DatabaseConnector.class, DatabaseConnectorRegistry.class.getClassLoader());
      for (final DatabaseConnector databaseConnector : serviceLoader) {
        final String databaseSystemIdentifier =
            databaseConnector.getDatabaseServerType().getDatabaseSystemIdentifier();
        try {
          LOGGER.log(
              Level.CONFIG,
              new StringFormat(
                  "Loading database connector, %s=%s",
                  databaseSystemIdentifier, databaseConnector.getClass().getName()));
          // Put in map
          databaseConnectorRegistry.put(databaseSystemIdentifier, databaseConnector);
        } catch (final Exception e) {
          LOGGER.log(
              Level.CONFIG,
              e,
              new StringFormat(
                  "Could not load database connector, %s=%s",
                  databaseSystemIdentifier, databaseConnector.getClass().getName()));
        }
      }
    } catch (final Exception e) {
      throw new InternalRuntimeException("Could not load database connector registry", e);
    }

    LOGGER.log(
        Level.CONFIG,
        new StringFormat("Loaded %d database connectors", databaseConnectorRegistry.size()));

    return databaseConnectorRegistry;
  }

  /**
   * Load registered database drivers, and throw exception if any driver cannot be loaded. Cycling
   * through the service loader and loading driver classes allows for dependencies to be vetted out.
   */
  private static void loadJdbcDrivers() {
    final boolean log = LOGGER.isLoggable(Level.CONFIG);
    int index = 0;
    final StringBuilder buffer = new StringBuilder(1024);
    try {
      buffer.append("Registered JDBC drivers:").append(System.lineSeparator());
      final ServiceLoader<Driver> serviceLoader = ServiceLoader.load(Driver.class);
      for (final Driver driver : serviceLoader) {
        index++;
        if (log) {
          buffer.append(String.format("%2d %50s", index, driver.getClass().getName()));
          try {
            buffer.append(
                String.format(" %2d.%d", driver.getMajorVersion(), driver.getMinorVersion()));
          } catch (final Exception e) {
            // Ignore exceptions from badly behaved drivers
          }
          buffer.append(System.lineSeparator());
        }
      }
    } catch (final Throwable e) {
      throw new InternalRuntimeException("Could not load database drivers", e);
    }
    if (log) {
      LOGGER.log(Level.CONFIG, buffer.toString());
    }
  }

  private final Map<String, DatabaseConnector> databaseConnectorRegistry;

  private DatabaseConnectorRegistry() {
    loadJdbcDrivers();
    databaseConnectorRegistry = loadDatabaseConnectorRegistry();
  }

  public DatabaseConnector findDatabaseConnector(final Connection connection) {
    try {
      checkConnection(connection);
      final String url = connection.getMetaData().getURL();
      return findDatabaseConnectorFromUrl(url);
    } catch (final SQLException e) {
      return DatabaseConnector.UNKNOWN;
    }
  }

  public DatabaseConnector findDatabaseConnectorFromDatabaseSystemIdentifier(
      final String databaseSystemIdentifier) {
    if (hasDatabaseSystemIdentifier(databaseSystemIdentifier)) {
      return databaseConnectorRegistry.get(databaseSystemIdentifier);
    } else {
      return DatabaseConnector.UNKNOWN;
    }
  }

  public DatabaseConnector findDatabaseConnectorFromUrl(final String url) {
    if (isBlank(url)) {
      return DatabaseConnector.UNKNOWN;
    }

    for (final DatabaseConnector databaseConnector : databaseConnectorRegistry.values()) {
      if (databaseConnector.supportsUrl(url)) {
        return databaseConnector;
      }
    }

    return DatabaseConnector.UNKNOWN;
  }

  public Collection<PluginCommand> getHelpCommands() {
    final Collection<PluginCommand> commandLineHelpCommands = new ArrayList<>();
    for (final DatabaseConnector databaseConnector : databaseConnectorRegistry.values()) {
      commandLineHelpCommands.add(databaseConnector.getHelpCommand());
    }
    return commandLineHelpCommands;
  }

  public boolean hasDatabaseSystemIdentifier(final String databaseSystemIdentifier) {
    return databaseConnectorRegistry.containsKey(databaseSystemIdentifier);
  }

  @Override
  public Iterator<DatabaseServerType> iterator() {
    final List<DatabaseServerType> databaseServerTypes = new ArrayList<>();
    for (final DatabaseConnector databaseConnector : databaseConnectorRegistry.values()) {
      databaseServerTypes.add(databaseConnector.getDatabaseServerType());
    }
    databaseServerTypes.sort(naturalOrder());
    return databaseServerTypes.iterator();
  }
}
